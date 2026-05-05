package org.example.agent.agent_base;

import com.alibaba.fastjson2.JSONObject;
import org.example.agent.*;
import org.example.agent.agent_callbacks.AgentCallback;
import org.example.agent.agent_commands.AgentCommand;
import org.example.agent.agent_hooks.AgentHook;
import org.example.models.AbstractModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * agent抽象父类:
 * 提供命令输入最开始的判断和部分回调机制
 */
public abstract class AbstractAgent implements IAgent, AgentCallback, IAgentToolUse, IAgentSkillUse, IAgentCallBackUse, IAgentHookUse, IAgentCommandUse {
    protected final AbstractModel model;
    protected final String agentName;

    protected final List<AgentCallback> agentCallbacks = new ArrayList<>();
    protected final List<AgentCommand> agentCommands = new ArrayList<>();
    protected final List<AgentHook> agentHooks = new ArrayList<>();

    public AbstractAgent(AbstractModel model, String agentName) {
        this.model = model;
        this.agentName = agentName;
    }

    @Override
    public String chatOrCommand(String content) throws IOException, InterruptedException {
        eachAtomicInitFirst(this);
        eachCheckSecond(this);
        if (content.startsWith("/")) {
            return command(content);
        } else {
            return agentLoop(content);
        }
    }

    protected String command(String content) throws IOException {
        Optional<AgentCommand> agentCommandOptional = agentCommands.stream().filter(cv -> cv.isSupportCommand(this, content)).findFirst();

        // 执行命令前回调
        callBeforeCommand(this, content);

        String commandRsp = getCommandRspWithOptionHook(content, agentCommandOptional);

        // 执行命令后回调
        callAfterCommand(this, content, commandRsp);

        return commandRsp;
    }

    private String getCommandRspWithOptionHook(String content, Optional<AgentCommand> agentCommandOptional) throws IOException {
        String commandRsp = null;
        for (AgentHook agentHook : agentHooks) {
            commandRsp = agentHook.hookCommand(this, content);
            if (commandRsp != null) {
                break;
            }
        }
        if (commandRsp == null) {
            commandRsp = agentCommandOptional.isPresent() ? agentCommandOptional.get().command(this, content) : "不支持当前命令，请确认后再次输入！";
        }
        return commandRsp;
    }

    /**
     * agentLoop，交给子类实现
     *
     * @param content 用户提示词
     * @return 返回
     */
    protected abstract String agentLoop(String content) throws IOException, InterruptedException;

    @Override
    public AbstractModel getModel() {
        return model;
    }

    @Override
    public String getAgentName() {
        return agentName;
    }

    @Override
    public void registryAgentCallback(AgentCallback agentCallback) {
        agentCallbacks.add(agentCallback);
    }

    @Override
    public void registryHook(AgentHook agentHook) {
        agentHooks.add(agentHook);
    }

    @Override
    public void registryCommand(AgentCommand agentCommand) {
        agentCommands.add(agentCommand);
    }

    @Override
    public void eachAtomicInitFirst(AbstractAgent agent) {
        agentCallbacks.forEach(cv -> cv.eachAtomicInitFirst(agent));
    }

    @Override
    public void eachCheckSecond(AbstractAgent agent) {
        agentCallbacks.forEach(cv -> cv.eachAtomicInitFirst(agent));
    }

    @Override
    public void callBeforeCommand(AbstractAgent agent, String content) {
        agentCallbacks.forEach(cv -> cv.callBeforeCommand(agent, content));
    }

    @Override
    public void callAfterCommand(AbstractAgent agent, String content, String commandRsp) {
        agentCallbacks.forEach(cv -> cv.callAfterCommand(agent, content, commandRsp));
    }

    @Override
    public void callAfterAddUserMessage(AbstractAgent agent, JSONObject userMessage) {
        agentCallbacks.forEach(cv -> cv.callAfterAddUserMessage(agent, userMessage));
    }

    @Override
    public void callBeforeChat(AbstractAgent agent) {
        agentCallbacks.forEach(cv -> cv.callBeforeChat(agent));
    }

    @Override
    public void callAfterChat(AbstractAgent agent, JSONObject chatRsp, JSONObject assistantMessage) {
        agentCallbacks.forEach(cv -> cv.callAfterChat(agent, chatRsp, assistantMessage));
    }

    @Override
    public void callBeforeToolsUse(AbstractAgent agent) {
        agentCallbacks.forEach(cv -> cv.callBeforeToolsUse(agent));
    }

    @Override
    public void callAfterToolsUse(AbstractAgent agent) {
        agentCallbacks.forEach(cv -> cv.callAfterToolsUse(agent));
    }

    @Override
    public void callBeforeToolUse(AbstractAgent agent, String id, String name, JSONObject arguments) {
        agentCallbacks.forEach(cv -> cv.callBeforeToolUse(agent, id, name, arguments));
    }

    @Override
    public void callAfterToolUse(AbstractAgent agent, String id, String name, JSONObject arguments, JSONObject toolMessage) {
        agentCallbacks.forEach(cv -> cv.callAfterToolUse(agent, id, name, arguments, toolMessage));
    }
}
