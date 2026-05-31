package org.example.framework_models;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.utils.HttpClientUtil;

import java.io.IOException;
import java.util.Map;

/**
 * OpenAi API格式
 */
public abstract class AbstractOpenAiModel extends AbstractModel {
    public AbstractOpenAiModel(String url, String apiKey, String model) {
        super(url, apiKey, model);
        setModel(model);

        setMessages(new JSONArray());
        setTools(new JSONArray());

        // 最大输出tokens
        setMaxTokens(getMaxInputTokens() / 10);
        // 思考模式
        setEnabledThink(true);
        // 思考等级
        setResoningEffort("high");
        // 返回响应格式
        setResponseFormat("text");
        // 推理温度
        setTemperature(1.0D);
        // 推理topP
        setTopP(1);
    }

    @Override
    public void setModel(String model) {
        super.setModel(model);
        curReq.put("model", model);
    }

    @Override
    public JSONObject chat() throws IOException, InterruptedException {
        return chatInline().getJSONArray("choices").getJSONObject(0);
    }

    /**
     * 使用当前提示词请求一次
     *
     * @return 请求响应
     * @throws IOException          io异常
     * @throws InterruptedException 线程等待中断
     */
    private JSONObject chatInline() throws IOException, InterruptedException {
        JSONObject result = HttpClientUtil.send(getUrl(), getApiKey(), curReq);

        JSONObject usage = result.getJSONObject("usage");
        promptTokensSum += (lastPromptTokens = usage.getInteger("prompt_tokens"));
        completionTokensSum += (lastCompletionTokens = usage.getInteger("completion_tokens"));
        totalTokensSum += (lastTotalTokens = usage.getInteger("total_tokens"));

        System.out.printf("请求url:%s, 模型:%s, 提示词token数:%d, 补全token数:%d, 总token数:%d %s", getUrl(), getModel(), lastPromptTokens, lastCompletionTokens, lastTotalTokens, System.lineSeparator());

        return result;
    }

    /**
     * 构造系统提示词
     *
     * @param content content
     */
    @Override
    public JSONObject addSystemMessages(String content) {
        JSONObject msg = message(content, "system");
        ((JSONArray) curReq.get("messages")).add(msg);
        return msg;
    }

    /**
     * 构造用户提示词
     *
     * @param content content
     */
    @Override
    public JSONObject addUserMessage(String content) {
        JSONObject msg = message(content, "user");
        ((JSONArray) curReq.get("messages")).add(msg);
        return msg;
    }

    /**
     * 构造工具提示词
     *
     * @param content    content
     * @param toolCallId toolCallId
     */
    @Override
    public JSONObject addToolMessage(String content, String toolCallId) {
        JSONObject msg = message(content, "tool");
        msg.put("tool_call_id", toolCallId);
        ((JSONArray) curReq.get("messages")).add(msg);
        return msg;
    }

    /**
     * 传入模型返回的助手提示词
     *
     * @param content content
     */
    @Override
    public void addAssistantMessages(JSONObject content) {
        ((JSONArray) curReq.get("messages")).add(content);
    }

    private JSONObject message(String content, String role) {
        JSONObject msg = new JSONObject();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }

    @Override
    public String extractToolName(JSONObject tool) {
        return tool.getJSONObject("function").getString("name");
    }

    @Override
    public JSONObject buildTool(JSONObject function) {
        JSONObject tool = new JSONObject();
        tool.put("type", "function");
        tool.put("function", function);
        return tool;
    }

    @Override
    public JSONObject buildToolFunction(String name, String desc, JSONObject parameters) {
        JSONObject function = new JSONObject();
        function.put("name", name);
        function.put("description", desc);
        function.put("parameters", parameters);
        return function;
    }

    @Override
    public JSONObject buildToolParameters(JSONObject properties, String[] required) {
        JSONObject function = new JSONObject();
        function.put("type", "object");
        function.put("properties", properties);
        function.put("required", required);
        return function;
    }

    @Override
    public JSONObject buildToolProperties(Map<String, JSONObject> properties) {
        return new JSONObject(properties);
    }

    @Override
    public JSONObject buildToolProperty(String type, String description, Object[] enums, JSONObject items) {
        JSONObject property = new JSONObject();
        property.put("type", type);
        property.put("description", description);
        property.put("enum", enums);
        if (!items.isEmpty()) {
            items.put("type", "object");
        }
        property.put("items", items);
        return property;
    }

    @Override
    public int getMaxTokens() {
        return curReq.getIntValue("max_tokens");
    }

    @Override
    public void setMaxTokens(int maxTokens) {
        curReq.put("max_tokens", maxTokens);
    }

    @Override
    public boolean isEnabledThink() {
        return curReq.getJSONObject("thinking").getString("type").equals("enabled");
    }

    @Override
    public void setEnabledThink(boolean isEnabledThink) {
        if (isEnabledThink) {
            curReq.put("thinking", new JSONObject(Map.of("type", "enabled")));
            setResoningEffort("high");
        } else {
            curReq.put("thinking", new JSONObject(Map.of("type", "disabled")));
            setResoningEffort("");
        }
    }

    @Override
    public double getTemperature() {
        return curReq.getDoubleValue("temperature");
    }

    /*
     * 采样温度，介于 0 和 2 之间。更高的值，如 0.8，会使输出更随机，而更低的值，如 0.2，会使其更加集中和确定。 我们通常建议可以更改这个值或者更改 top_p，但不建议同时对两者进行修改。
     */
    @Override
    public void setTemperature(double temperature) {
        curReq.put("temperature", temperature);
    }

    @Override
    public int getTopP() {
        return curReq.getIntValue("top_p");
    }

    /*
     * 作为调节采样温度的替代方案，模型会考虑前 top_p 概率的 token 的结果。所以 0.1 就意味着只有包括在最高 10% 概率中的 token 会被考虑。 我们通常建议修改这个值或者更改 temperature，但不建议同时对两者进行修改。
     */
    @Override
    public void setTopP(int topP) {
        curReq.put("top_p", topP);
    }

    @Override
    public String getResponseFormat() {
        return curReq.getJSONObject("response_format").getString("type");
    }

    @Override
    public void setResponseFormat(String format) {
        curReq.put("response_format", new JSONObject(Map.of("type", format)));
    }

    @Override
    public String getResoningEffort() {
        return curReq.getString("reasoning_effort", "high");
    }

    @Override
    public void setResoningEffort(String effort) {
        curReq.put("reasoning_effort", effort);
    }

    @Override
    public JSONArray getTools() {
        return curReq.getJSONArray("tools");
    }

    @Override
    public void setTools(JSONArray tools) {
        curReq.put("tools", tools);
    }

    @Override
    public JSONArray getMessages() {
        return curReq.getJSONArray("messages");
    }

    @Override
    public void setMessages(JSONArray messages) {
        curReq.put("messages", messages);
    }
}
