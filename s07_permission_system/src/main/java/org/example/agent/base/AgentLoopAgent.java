package org.example.agent.base;

import com.alibaba.fastjson2.JSONObject;
import org.example.agent.callbacks.AgentCallback;
import org.example.models.AbstractModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * agent抽象父类:
 * 提供最根本的chatOrCommand实现
 * 提供AgentCallback的回调机制
 */
public abstract class AgentLoopAgent implements IAgent, AgentCallback {
    protected final AbstractModel model;
    protected final String agentName;
    protected final List<AgentCallback> agentCallbacks = new ArrayList<>();

    public AgentLoopAgent(AbstractModel model, String agentName) {
        this.model = model;
        this.agentName = agentName;
    }

    @Override
    public String chatOrCommand(String content) throws IOException, InterruptedException {
        eachAtomicInitFirst(this);
        eachCheckSecond(this);
        return agentLoop(content);
    }

    private String agentLoop(String content) throws IOException, InterruptedException {
        JSONObject userMessage = model.addUserMessage(content);
        callAfterAddUserMessage(this, userMessage);

        while (true) {
            // chat前回调
            callBeforeChat(this);

            JSONObject chatRsp = model.chat();
            JSONObject message = chatRsp.getJSONObject("message");
            model.addAssistantMessages(message);

            // chat后回调
            callAfterChat(this, chatRsp, message);

            // 非工具调用即刻返回
            if (!chatRsp.getString("finish_reason").equals("tool_calls")) {
                return message.getString("content");
            }

            // 工具使用前回调
            callBeforeToolsUse(this);
            // 依次调用tools
            message.getJSONArray("tool_calls").forEach(obj -> toolUse((JSONObject) obj));
            // 工具使用后回调
            callAfterToolsUse(this);
        }
    }

    /**
     * 工具使用，交给子类实现
     *
     * @param obj llm返回的tool调用rsp
     */
    protected abstract void toolUse(JSONObject obj);

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
    public void eachAtomicInitFirst(IAgent agent) {
        agentCallbacks.forEach(cv -> cv.eachAtomicInitFirst(agent));
    }

    @Override
    public void eachCheckSecond(IAgent agent) {
        agentCallbacks.forEach(cv -> cv.eachAtomicInitFirst(agent));
    }

    @Override
    public void callAfterAddUserMessage(IAgent agent, JSONObject userMessage) {
        agentCallbacks.forEach(cv -> cv.callAfterAddUserMessage(agent, userMessage));
    }

    @Override
    public void callBeforeChat(IAgent agent) {
        agentCallbacks.forEach(cv -> cv.callBeforeChat(agent));
    }

    @Override
    public void callAfterChat(IAgent agent, JSONObject chatRsp, JSONObject assistantMessage) {
        agentCallbacks.forEach(cv -> cv.callAfterChat(agent, chatRsp, assistantMessage));
    }

    @Override
    public void callBeforeToolsUse(IAgent agent) {
        agentCallbacks.forEach(cv -> cv.callBeforeToolsUse(agent));
    }

    @Override
    public void callAfterToolsUse(IAgent agent) {
        agentCallbacks.forEach(cv -> cv.callAfterToolsUse(agent));
    }

    @Override
    public void callBeforeToolUse(IAgent agent, String id, String name, JSONObject arguments) {
        agentCallbacks.forEach(cv -> cv.callBeforeToolUse(agent, id, name, arguments));
    }

    @Override
    public void callAfterToolUse(IAgent agent, String id, String name, JSONObject arguments, JSONObject toolMessage) {
        agentCallbacks.forEach(cv -> cv.callAfterToolUse(agent, id, name, arguments, toolMessage));
    }
}
