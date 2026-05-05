package org.example.models;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.utils.HttpClientUtil;
import org.example.utils.JsonCloneUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * model抽象父类，提供公共方法
 */
public abstract class AbstractModel implements IModel, ModelSetting, ModelToolUse, ModelClone<AbstractModel>, ModelMonitor {
    public JSONObject curReq = new JSONObject();
    protected Set<String> toolsSet = new HashSet<>();

    private long lastPromptTokens = 0;
    private long lastCompletionTokens = 0;
    private long lastTotalTokens = 0;

    private long promptTokensSum = 0;
    private long completionTokensSum = 0;
    private long totalTokensSum = 0;

    public AbstractModel() {
        curReq.put("messages", new JSONArray());
        curReq.put("tools", new JSONArray());
    }

    /**
     * 使用当前提示词请求一次
     *
     * @return 请求响应
     * @throws IOException          io异常
     * @throws InterruptedException 线程等待中断
     */
    @Override
    public JSONObject chat() throws IOException, InterruptedException {
        JSONObject result = HttpClientUtil.send(getUrl(), getApiKey(), curReq);

        JSONObject usage = result.getJSONObject("usage");
        promptTokensSum += (lastPromptTokens = usage.getInteger("prompt_tokens"));
        completionTokensSum += (lastCompletionTokens = usage.getInteger("completion_tokens"));
        totalTokensSum += (lastTotalTokens = usage.getInteger("total_tokens"));

        System.out.printf("请求url:%s, 模型:%s, 提示词token数:%d, 补全token数:%d, 总token数:%d %s", getUrl(), getModel(), lastPromptTokens, lastCompletionTokens, lastTotalTokens, System.lineSeparator());

        return result;
    }

    @Override
    public long getLastChatTotalTokens() {
        return lastTotalTokens;
    }

    @Override
    public long getLastChatPromptTokens() {
        return lastPromptTokens;
    }

    @Override
    public long getLastChatCompletionTokens() {
        return lastCompletionTokens;
    }

    @Override
    public long getTotalTokensSum() {
        return totalTokensSum;
    }

    @Override
    public long getPromptTokensSum() {
        return promptTokensSum;
    }

    @Override
    public long getCompletionTokensSum() {
        return completionTokensSum;
    }

    @Override
    public AbstractModel cloneWithHistory(AbstractModel newModel) {
        newModel.curReq = JsonCloneUtil.deepClone(curReq);
        newModel.toolsSet.addAll(toolsSet);
        return newModel;
    }

    @Override
    public AbstractModel cloneWithoutHistory(AbstractModel newModel) {
        newModel.toolsSet.addAll(toolsSet);
        return newModel;
    }

    /**
     * 添加tool
     *
     * @param tool tool
     */
    @Override
    public void addTool(JSONObject tool) {
        String toolName = extractToolName(tool);
        if (!toolsSet.contains(toolName)) {
            ((JSONArray) curReq.get("tools")).add(tool);
            toolsSet.add(toolName);
        }
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
}
