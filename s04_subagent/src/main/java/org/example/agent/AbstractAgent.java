package org.example.agent;

import com.alibaba.fastjson2.JSONObject;
import org.example.models.AbstractModel;
import org.example.tool.ToolExecuter;
import org.example.tool.ToolResolve;
import org.example.utils.ToolToModelTransformUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * agent抽象父类，提供公共方法，定义架构
 */
public abstract class AbstractAgent implements IAgent {
    protected final Map<String, ToolExecuter> toolHandlers = new HashMap<>();
    protected final AbstractModel model;
    protected final String agentName;

    public AbstractAgent(AbstractModel model, String agentName) {
        this.model = model;
        this.agentName = agentName;
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

    private void toolUse(JSONObject obj) {
        JSONObject function = obj.getJSONObject("function");

        // tool参数
        String id = obj.getString("id");
        String name = function.getString("name");
        JSONObject arguments = JSONObject.parse(function.getString("arguments"));

        // 工具使用前回调
        callBeforeToolUse(id, name, arguments);

        System.out.printf("开始执行tool, id:%s, func:%s, args:%s %s", id, name, arguments, System.lineSeparator());
        String toolRsp = toolHandlers.get(name).execute(arguments);
        System.out.printf("结束执行tool, id:%s, func:%s, args:%s , result:%s %s", id, name, arguments, toolRsp, System.lineSeparator());
        model.addToolMessages(toolRsp, id);

        // 工具使用后回调
        callAfterToolUse(id, name, arguments);
    }

    /**
     * 单个工具使用前回调
     *
     * @param id        tool id
     * @param name      tool name
     * @param arguments tool args
     */
    public void callAfterToolUse(String id, String name, JSONObject arguments) {
    }

    /**
     * 单个工具使用后回调
     *
     * @param id        tool id
     * @param name      tool name
     * @param arguments tool args
     */
    public void callBeforeToolUse(String id, String name, JSONObject arguments) {
    }

    /**
     * tools使用前回调
     */
    public void callBeforeToolsUse() {
    }

    /**
     * tools使用后回调
     */
    public void callAfterToolsUse() {
    }

    /**
     * llm chat前回调
     */
    public void callBeforeChat() {
    }

    /**
     * llm chat后回调
     *
     * @param message llm响应
     */
    public void callAfterChat(JSONObject message) {
        System.out.printf("%s>>>%s%s", agentName, message.getString("content"), System.lineSeparator());
    }

    /**
     * 工具注册
     *
     * @param toolObj 实例类型tool
     */
    public void registryTool(Object toolObj) {
        registryTool(ToolResolve.resolve(toolObj));
    }

    /**
     * 工具注册
     *
     * @param toolObj 静态方法类型tool
     */
    public void registryTool(Class<?> toolObj) {
        registryTool(ToolResolve.resolve(toolObj));
    }

    private void registryTool(List<ToolResolve.ToolResolveResult> toolResolveResults) {
        for (ToolResolve.ToolResolveResult toolResolveResult : toolResolveResults) {
            toolHandlers.put(toolResolveResult.name(), toolResolveResult.toolHandler());
            model.addTool(ToolToModelTransformUtil.transform(toolResolveResult, model));
        }
    }
}
