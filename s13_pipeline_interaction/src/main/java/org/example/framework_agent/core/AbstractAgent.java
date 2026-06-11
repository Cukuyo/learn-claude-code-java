package org.example.framework_agent.core;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.framework_agent.*;
import org.example.framework_models.AbstractModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * agent抽象父类:
 * 提供最根本的chatOrCommand实现
 * 提供回调机制
 * 提供命令机制
 * 提供hook机制
 */
public abstract class AbstractAgent implements IAgent, AgentCallback, IAgentToolUse, IAgentSkillUse, IAgentCallBackUse, IAgentHookUse, IAgentCommandUse {
    public final AbstractModel model;
    public final String agentName;
    public final String agentRole;

    public final List<AgentCallback> agentCallbacks = new ArrayList<>();
    public final List<AgentCommand> agentCommands = new ArrayList<>();
    public final List<AgentHook> agentHooks = new ArrayList<>();

    public AbstractAgent(AbstractModel model, String agentName, String agentRole) {
        this.model = model;
        this.agentName = agentName;
        this.agentRole = agentRole;

        model.addSystemMessages(agentRole);
    }

    @Override
    public String chatOrCommand(String content) throws IOException, InterruptedException {
        eachCheckWithContent(this, content);
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
        // 执行带可能hook的命令
        String commandRsp = getCommandRspWithOptionHook(content, agentCommandOptional);
        // 执行命令后回调
        callAfterCommand(this, content, commandRsp);

        return commandRsp;
    }

    protected String getCommandRspWithOptionHook(String content, Optional<AgentCommand> agentCommandOptional) throws IOException {
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
    public String getAgentRole() {
        return agentRole;
    }

    @Override
    public void registryAgentCallback(AgentCallback agentCallback) {
        agentCallbacks.add(agentCallback);
        agentCallback.initSelf(this);
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
    public void eachCheckWithContent(AbstractAgent agent, String content) {
        agentCallbacks.forEach(cv -> cv.eachCheckWithContent(agent, content));
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
    public void callBeforeAgentLoop(AbstractAgent agent, JSONArray messages, JSONObject userMessage) {
        agentCallbacks.forEach(cv -> cv.callBeforeAgentLoop(agent, messages, userMessage));
    }

    @Override
    public void callAfterAgentLoop(AbstractAgent agent, JSONArray messages, JSONObject userMessage, String chatRsp) {
        agentCallbacks.forEach(cv -> cv.callAfterAgentLoop(agent, messages, userMessage, chatRsp));
    }

    @Override
    public void callBeforeChat(AbstractAgent agent) {
        agentCallbacks.forEach(cv -> cv.callBeforeChat(agent));
    }

    @Override
    public void callAfterChat(AbstractAgent agent, JSONObject chatRsp, JSONObject assistantMessage, boolean finished) {
        agentCallbacks.forEach(cv -> cv.callAfterChat(agent, chatRsp, assistantMessage, finished));
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
