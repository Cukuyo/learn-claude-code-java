package org.example.agent.common;

import com.alibaba.fastjson2.JSONObject;
import org.example.models.AbstractModel;

import java.io.IOException;

/**
 * agent抽象父类，提供公共方法，定义架构
 */
public abstract class BaseAgent implements IAgent {
    public final AbstractModel model;
    public final String agentName;

    public BaseAgent(AbstractModel model, String agentName) {
        this.model = model;
        this.agentName = agentName;

        model.addSystemMessages("你当前的工作目录是<" + System.getProperty("user.dir") + ">，注意不要做出范围之外的危险行为！");
    }

    @Override
    public String chatOrCommand(String content) throws IOException, InterruptedException {
        return agentLoop(content);
    }

    private String agentLoop(String content) throws IOException, InterruptedException {
        model.addUserMessage(content);

        while (true) {
            // llm请求前回调
            callBeforeChat();

            JSONObject chatRsp = model.chat();
            JSONObject message = chatRsp.getJSONObject("message");
            // 每次都需要记录LLM的响应
            model.addAssistantMessages(message);

            // llm请求后回调
            callAfterChat(message);

            // 非工具调用即刻返回
            if (!chatRsp.getString("finish_reason").equals("tool_calls")) {
                return message.getString("content");
            }

            // 工具使用前回调
            callBeforeToolsUse();
            // 依次调用tools
            message.getJSONArray("tool_calls").forEach(obj -> toolUse((JSONObject) obj));
            // 工具使用后回调
            callAfterToolsUse();
        }
    }

    protected abstract void toolUse(JSONObject obj);

    /**
     * tools使用前回调
     */
    protected void callBeforeToolsUse() {
    }

    /**
     * tools使用后回调
     */
    protected void callAfterToolsUse() {
    }

    /**
     * llm chat前回调
     */
    protected void callBeforeChat() {
    }

    /**
     * llm chat后回调
     *
     * @param message llm响应
     */
    protected void callAfterChat(JSONObject message) {
        System.out.printf("%s>>>%s%s", agentName, message.getString("content"), System.lineSeparator());
    }
}
