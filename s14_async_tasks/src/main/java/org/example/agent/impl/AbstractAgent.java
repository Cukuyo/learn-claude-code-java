package org.example.agent.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.agent.AgentCallback;
import org.example.agent.AgentCommand;
import org.example.agent.AgentHook;
import org.example.agent.IAgent;
import org.example.agent.IAgentCallbackUse;
import org.example.agent.IAgentCommandUse;
import org.example.agent.IAgentHookUse;
import org.example.agent.IAgentSkillUse;
import org.example.agent.IAgentToolUse;
import org.example.models.AbstractModel;

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
public abstract class AbstractAgent implements IAgent, AgentCallback, IAgentToolUse, IAgentSkillUse, IAgentCallbackUse, IAgentHookUse, IAgentCommandUse {
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

        model.addSystemMessages("你的名字是" + agentName);
        model.addSystemMessages(agentRole);
    }

    @Override
    public String chatOrCommand(String content) throws IOException, InterruptedException {
        if (content.startsWith("/")) {
            return command(content);
        } else {
            return chat(content);
        }
    }

    @Override
    public String command(String command) {
        return command("", command);
    }

    @Override
    public String command(String name, String command) {
        Optional<AgentCommand> agentCommandOptional = agentCommands.stream().filter(cv -> cv.isSupportCommand(this, command)).findFirst();

        // 执行命令前回调
        callBeforeCommand(this, command);
        // 执行带可能hook的命令
        String commandRsp = getCommandRspWithOptionHook(name, command, agentCommandOptional);
        // 执行命令后回调
        callAfterCommand(this, command, commandRsp);

        return commandRsp;
    }

    protected String getCommandRspWithOptionHook(String name, String content, Optional<AgentCommand> agentCommandOptional) {
        String commandRsp = null;
        for (AgentHook agentHook : agentHooks) {
            commandRsp = agentHook.hookCommand(this, name, content);
            if (commandRsp != null) {
                break;
            }
        }
        if (commandRsp == null) {
            commandRsp = agentCommandOptional.isPresent() ? agentCommandOptional.get().command(this, content) : "不支持当前命令，请确认后再次输入！";
        }
        return commandRsp;
    }

    @Override
    public String chat(String chatContent) throws IOException, InterruptedException {
        return chat(List.of(""), List.of(chatContent));
    }

    @Override
    public String chat(List<String> nameList, List<String> chatContentList) throws IOException, InterruptedException {
        List<JSONObject> userMessageList = new ArrayList<>();
        for (int i = 0; i < chatContentList.size(); i++) {
            String name = nameList.get(i);
            String chatContent = chatContentList.get(i);

            // 添加User提示词后回调
            JSONObject userMessage = addUserMessageWithOptionHook(name, chatContent);
            userMessageList.add(userMessage);
        }

        callBeforeAgentLoop(this, getModel().getMessages(), userMessageList);
        String rsp = agentLoop();
        callAfterAgentLoop(this, getModel().getMessages(), userMessageList, rsp);
        return rsp;
    }

    protected JSONObject addUserMessageWithOptionHook(String name, String content) {
        JSONObject userMessage = null;
        for (AgentHook agentHook : agentHooks) {
            userMessage = agentHook.hookAddUserMessage(this, name, content);
            if (userMessage != null) {
                break;
            }
        }
        if (userMessage == null) {
            userMessage = model.addUserMessage(content, name);
        }
        return userMessage;
    }

    /**
     * agentLoop，交给子类实现
     *
     * @return 返回
     */
    protected abstract String agentLoop() throws IOException, InterruptedException;

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
    public void callBeforeCommand(AbstractAgent agent, String content) {
        agentCallbacks.forEach(cv -> cv.callBeforeCommand(agent, content));
    }

    @Override
    public void callAfterCommand(AbstractAgent agent, String content, String commandRsp) {
        agentCallbacks.forEach(cv -> cv.callAfterCommand(agent, content, commandRsp));
    }

    @Override
    public void callBeforeAgentLoop(AbstractAgent agent, JSONArray messages, List<JSONObject> userMessageList) {
        agentCallbacks.forEach(cv -> cv.callBeforeAgentLoop(agent, messages, userMessageList));
    }

    @Override
    public void callAfterAgentLoop(AbstractAgent agent, JSONArray messages, List<JSONObject> userMessageList, String chatRsp) {
        agentCallbacks.forEach(cv -> cv.callAfterAgentLoop(agent, messages, userMessageList, chatRsp));
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
