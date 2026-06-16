package org.example.agents.persist;

import com.alibaba.fastjson2.JSONObject;
import org.example.agents.persist.systems.permission.*;
import org.example.agent.AgentCommand;
import org.example.agent.AgentHook;
import org.example.agent.impl.AbstractAgent;
import org.example.utils.DialogUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * 权限系统，为hook形式，支持命令修改
 */
public class PermissionSystem implements AgentHook, AgentCommand {
    private final List<AgentCommand> agentCommands = new ArrayList<>();

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
            return ask(agent, name, command, denyRule);
        }
    }

    private PermissionRule matchedPermissionRule(String name, String content, Map<String, List<PermissionRule>> map) {
        List<PermissionRule> ruleList = map.getOrDefault(name, new ArrayList<>());
        for (PermissionRule rule : ruleList) {
            try {
                if (rule.pattern.matcher(content).find()) {
                    return rule;
                }
            } catch (NullPointerException e) {
                throw e;
            }
        }


        return null;
    }

    private String ask(AbstractAgent agent, String toolName, String command, PermissionRule denyRule) {
        String title = "危险操作确认";
        String desc = String.format("%s正在执行危险操作：%s : %s，请确认是否允许", agent.agentName, toolName, command);
        Object selectValue = DialogUtil.showDangerConfirmDialog(PermissionAskBehavior.values(), title, desc, PermissionAskBehavior.NO);
        PermissionAskBehavior userRsp = PermissionAskBehavior.valueOf(selectValue.toString());
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

    @Override
    public String desc(AbstractAgent agent) {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        for (AgentCommand agentCommand : agentCommands) {
            joiner.add(agentCommand.desc(agent));
        }
        return joiner.toString();
    }

    @Override
    public String help(AbstractAgent agent) {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        for (AgentCommand agentCommand : agentCommands) {
            joiner.add(agentCommand.help(agent));
        }
        return joiner.toString();
    }

    @Override
    public boolean isSupportCommand(AbstractAgent agent, String cmd) {
        return agentCommands.stream().anyMatch(cv -> cv.isSupportCommand(agent, cmd));
    }

    @Override
    public String command(AbstractAgent agent, String cmd) {
        return agentCommands.stream().filter(cv -> cv.isSupportCommand(agent, cmd)).findFirst().orElse(AgentCommand.EMPTY).command(agent, cmd);
    }

    private class ModeCommand implements AgentCommand {
        @Override
        public String desc(AbstractAgent agent) {
            return "/mode 权限模式";
        }

        @Override
        public String help(AbstractAgent agent) {
            return """
                    /mode 显示当前模式
                    /mode help 显示帮助信息
                    """ +
                    "/mode" + " " + Arrays.toString(PermissionMode.values()) + " 切换为新模式" +
                    System.lineSeparator();
        }

        @Override
        public boolean isSupportCommand(AbstractAgent agent, String cmd) {
            return cmd.trim().split("\\s+")[0].equals("/mode");
        }

        @Override
        public String command(AbstractAgent agent, String cmd) {
            String[] arr = cmd.trim().split("\\s+");
            for (int i = 0; i < arr.length; i++) {
                arr[i] = arr[i].trim();
            }

            if (arr.length == 1) {
                return "当前模式为 " + mode;
            }
            if (arr.length == 2 && arr[1].equals("help")) {
                return help(agent);

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
        public String desc(AbstractAgent agent) {
            return "/rules 权限规则";
        }

        @Override
        public String help(AbstractAgent agent) {
            return """
                    /rules 显示当前权限规则
                    /rules help 显示帮助信息
                    /rules flush 持久化当前权限规则
                    """;
        }

        @Override
        public boolean isSupportCommand(AbstractAgent agent, String cmd) {
            return cmd.trim().split("\\s+")[0].equals("/rules");
        }

        @Override
        public String command(AbstractAgent agent, String cmd) {
            String[] arr = cmd.trim().split("\\s+");
            for (int i = 0; i < arr.length; i++) {
                arr[i] = arr[i].trim();
            }

            if (arr.length == 1) {
                return show();
            }

            if (arr.length == 2 && arr[1].equals("help")) {
                return help(agent);
            }

            if (arr.length == 2 && arr[1].equals("flush")) {
                return flush();
            }
            return "不支持的命令参数！";
        }

        private String show() {
            StringBuilder builder = new StringBuilder(denyProps.size() * 256);
            denyProps.values().forEach(list -> list.forEach(rule -> builder.append(rule.toString()).append(System.lineSeparator())));
            return builder.toString();
        }

        private String flush() {
            List<PermissionRule> list = new LinkedList<>();
            denyProps.values().forEach(list::addAll);
            try {
                PermissionFileUtil.write(denyPath, list);
                return "已保存当前已允许的命令到 " + denyPath;
            } catch (IOException e) {
                return "保存当前已允许的命令到" + denyPath + "失败！ERROR:  " + e.getMessage();
            }
        }
    }
}
