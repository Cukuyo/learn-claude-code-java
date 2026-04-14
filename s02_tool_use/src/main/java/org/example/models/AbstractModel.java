package org.example.models;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import org.example.tool.ToolExecuter;
import org.example.tool.ToolResolve;
import org.example.utils.HttpClientUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * model抽象父类，提供公共方法
 */
public abstract class AbstractModel {
    private final JSONObject curReq = new JSONObject();
    private final String model;

    public AbstractModel(String model) {
        this.model = model;
        curReq.put("model", model);
        curReq.put("messages", new JSONArray());
        curReq.put("frequency_penalty", 0);
        curReq.put("max_tokens", 4096);
        curReq.put("presence_penalty", 0);
        curReq.put("top_p", 1);
        curReq.put("tools", new JSONArray());
    }

    abstract String getUrl();

    abstract String getApiKey();

    /**
     * 使用当前提示词请求一次
     *
     * @return 请求响应
     * @throws IOException          io异常
     * @throws InterruptedException 线程等待中断
     */
    public JSONObject chat() throws IOException, InterruptedException {
        JSONObject result = HttpClientUtil.send(getUrl(), getApiKey(), curReq);

        System.out.println(result);
        JSONObject usage = result.getJSONObject("usage");
        System.out.printf("请求url:%s, 模型:%s, 提示词token数:%d, 补全token数:%d, 总token数:%d %s",
                getUrl(), model,
                usage.getInteger("prompt_tokens"), usage.getInteger("completion_tokens"), usage.getInteger("total_tokens"),
                System.lineSeparator());

        return result;
    }

    /**
     * 添加方法
     * "name": "get_weather",
     * "description": "Get weather of a location, the user should supply a location first.",
     * "parameters": {
     * "type": "object",
     * "properties": {
     * "location": {
     * "type": "string",
     * "description": "The city and state, e.g. San Francisco, CA",
     * }
     * },
     * "required": ["location"]
     * }
     *
     * @param name       函数名
     * @param desc       描述
     * @param properties properties。0-变量名，1-类型，2-描述，3-是否必填
     */
    public void addTool(String name, String desc, String[][] properties) {
        JSONObject tool = new JSONObject();
        tool.put("type", "function");
        tool.put("function", buildFunction(name, desc, properties));
        ((JSONArray) curReq.get("tools")).add(tool);
    }

    private JSONObject buildFunction(String name, String desc, String[][] properties) {
        JSONObject function = new JSONObject();
        function.put("name", name);
        function.put("description", desc);
        function.put("parameters", buildParameters(properties));
        return function;
    }

    private JSONObject buildParameters(String[][] properties) {
        JSONObject propertiesJson = new JSONObject();
        JSONArray requiredJson = new JSONArray();
        for (String[] property : properties) {
            JSONObject propertyJson = new JSONObject();
            propertyJson.put("type", property[1]);
            propertyJson.put("description", property[2]);
            propertiesJson.put(property[0], propertyJson);
            if (property[3].equals("true")) {
                requiredJson.add(property[0]);
            }
        }

        JSONObject parameters = new JSONObject();
        parameters.put("type", "object");
        parameters.put("properties", propertiesJson);
        parameters.put("required", requiredJson);
        return parameters;
    }

    /**
     * 构造系统提示词
     *
     * @param content content
     */
    public void addSystemMessages(String content) {
        ((JSONArray) curReq.get("messages")).add(message(content, "system"));
    }

    /**
     * 构造用户提示词
     *
     * @param content content
     */
    public void addUserMessage(String content) {
        ((JSONArray) curReq.get("messages")).add(message(content, "user"));
    }

    /**
     * 构造工具提示词
     *
     * @param content    content
     * @param toolCallId toolCallId
     */
    public void addToolMessages(String content, String toolCallId) {
        JSONObject msg = message(content, "tool");
        msg.put("tool_call_id", toolCallId);
        ((JSONArray) curReq.get("messages")).add(msg);
    }

    /**
     * 传入模型返回的助手提示词
     *
     * @param content content
     */
    public void addAssistantMessages(JSONObject content) {
        ((JSONArray) curReq.get("messages")).add(content);
    }

    private JSONObject message(String content, String role) {
        JSONObject msg = new JSONObject();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }
}
