package org.example.agent.agent_unions;

import com.alibaba.fastjson2.JSONObject;
import org.example.agent.agent_base.AbstractAgent;
import org.example.agent.agent_commands.AgentCommand;
import org.example.agent.agent_hooks.AgentHook;
import org.example.permission.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * 权限系统，为hook形式，支持命令修改
 */
public class PermissionSystem implements AgentHook, AgentCommand {
    private List<AgentCommand> agentCommands = new ArrayList<>();

    public PermissionMode mode = PermissionMode.AUTO;

    public Path denyPath;
    public Map<String, List<PermissionRule>> denyProps = new HashMap<>();

    public PermissionSystem(Path denyPath) throws IOException {
        this.denyPath = denyPath;
        for (PermissionRule rule : PermissionFileUtil.read(denyPath)) {
            denyProps.computeIfAbsent(rule.toolName, _ -> new ArrayList<>()).add(rule);
        }

        agentCommands.add(new ModeCommand());
        agentCommands.add(new RulesCommand());
    }

    @Override
    public String hookToolUse(AbstractAgent agent, String id, String name, JSONObject arguments) {
        if (!denyProps.containsKey(name)) {
            return null;
        }

        String command = "";
        switch (name) {
            case "execute":
                command = arguments.getString("command");
                break;
            default:
                break;
        }

        PermissionRule denyRule = matchedPermissionRule(name, command, denyProps);
        // 没有匹配规则时返回，代表不是高危命令
        if (denyRule == null) {
            return null;
        }

        PermissionBehavior behavior = denyRule.getBehavior(mode);
        String behaviorReason = denyRule.getBehaviorReason(mode);
        if (behavior == PermissionBehavior.DENY) {
            return behaviorReason;
        } else if (behavior == PermissionBehavior.ALLOW) {
            return null;
        } else {// 需要询问用户
            return ask(name, command, denyRule);
        }
    }

    private String ask(String name, String command, PermissionRule denyRule) {
        System.out.printf("agent正在执行危险操作：%s : %s，请确认是否允许，可选输入为%s %s",
                name, command, Arrays.toString(PermissionAskBehavior.values()), System.lineSeparator());
        PermissionAskBehavior userRsp = PermissionAskBehavior.valueOf(new Scanner(System.in).nextLine().trim());
        switch (userRsp) {
            case ALWAYS -> {
                denyRule.update(mode, PermissionBehavior.ALLOW, "用户已确认允许执行此高危命令！");
                return null;
            }
            case NO -> {
                denyRule.update(mode, PermissionBehavior.DENY, "用户禁止此高危操作！");
                return denyRule.getBehaviorReason(mode);
            }
            default -> {
                return null;
            }
        }
    }

    private PermissionRule matchedPermissionRule(String name, String content, Map<String, List<PermissionRule>> map) {
        List<PermissionRule> ruleList = map.getOrDefault(name, new ArrayList<>());
        for (PermissionRule rule : ruleList) {
            if (rule.pattern.matcher(content).hasMatch()) {
                return rule;
            }
        }
        return null;
    }

    @Override
    public boolean isSupportCommand(AbstractAgent agent, String cmd) {
        return agentCommands.stream().anyMatch(cv -> cv.isSupportCommand(agent, cmd));
    }

    @Override
    public String command(AbstractAgent agent, String cmd) throws IOException {
        return agentCommands.stream().filter(cv -> cv.isSupportCommand(agent, cmd)).findFirst().orElse(AgentCommand.EMPTY).command(agent, cmd);
    }

    private class ModeCommand implements AgentCommand {
        @Override
        public boolean isSupportCommand(AbstractAgent agent, String cmd) {
            return cmd.startsWith("/mode");
        }

        @Override
        public String command(AbstractAgent agent, String cmd) throws IOException {
            String[] arr = cmd.trim().split("\\s+");
            if (arr.length == 1) {
                return "当前模式为 " + mode;
            }
            if (arr.length == 2) {
                mode = PermissionMode.valueOf(arr[1].toUpperCase());
                return "已切换模式为 " + mode;
            }
            return "不支持的命令参数！";
        }
    }

    private class RulesCommand implements AgentCommand {
        @Override
        public boolean isSupportCommand(AbstractAgent agent, String cmd) {
            return cmd.startsWith("/rules");
        }

        @Override
        public String command(AbstractAgent agent, String cmd) throws IOException {
            String[] arr = cmd.trim().split("\\s+");
            if (arr.length == 1) {
                StringBuilder builder = new StringBuilder(denyProps.size() * 256);
                builder.append("当前已禁止的命令为").append(System.lineSeparator());
                denyProps.values().forEach(list -> list.forEach(rule ->
                        builder.append(rule.toString()).append(System.lineSeparator())));
                return builder.toString();
            }
            if (arr.length == 2) {
                List<PermissionRule> list = new LinkedList<>();
                denyProps.values().forEach(list::addAll);
                PermissionFileUtil.write(denyPath, list);
                return "已保存当前已允许的命令到 " + denyPath;
            }
            return "不支持的命令参数！";
        }
    }
}
