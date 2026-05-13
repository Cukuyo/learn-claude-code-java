package org.example.framework_models;

import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

/**
 * Deepseek客户端
 */
public class DeepseekModel extends OpenAiModel {
    private static final int MAX_INPUT_TOKENS = 100 * 10000;
    private static final int MAX_OUTPUT_TOKENS = 384 * 1000;

    private String url;
    private String apiKey;
    private String model;

    public DeepseekModel(String apiKey) {
        this("https://api.deepseek.com/chat/completions", apiKey);
    }

    public DeepseekModel(String url, String apiKey) {
        this("deepseek-v4-flash", url, apiKey);
    }

    public DeepseekModel(String model, String url, String apiKey) {
        super();
        this.url = url;
        this.apiKey = apiKey;
        this.model = model;
        curReq.put("model", model);
        
        // 最大输出tokens
        curReq.put("max_tokens", MAX_OUTPUT_TOKENS/10);
        // 思考模式
        curReq.put("thinking", new JSONObject(Map.of("type", "enabled")));
        // 思考等级
        curReq.put("reasoning_effort", "high");
        // 返回响应格式
        curReq.put("response_format", new JSONObject(Map.of("type", "json_object")));
        // 推理温度
        curReq.put("temperature", 1);
        // 推理topP
        curReq.put("top_p", 1);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public int getMaxInputTokens() {
        return MAX_INPUT_TOKENS;
    }

    @Override
    public int getMaxOutTokens() {
        return MAX_OUTPUT_TOKENS;
    }

    @Override
    public AbstractModel cloneWithHistory() {
        return cloneWithHistory(new DeepseekModel(model, url, apiKey));
    }

    @Override
    public AbstractModel cloneWithoutHistory() {
        return cloneWithoutHistory(new DeepseekModel(model, url, apiKey));
    }

    @Override
    public void setUrl(String url) {
        this.url=url;
    }

    @Override
    public void setApiKey(String apiKey) {
        this.apiKey=apiKey;
    }

    @Override
    public void setModel(String model) {
        this.model=model;
    }

    @Override
    public int getMaxTokens() {
        return curReq.getIntValue("max_tokens", 1);
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
        curReq.put("thinking", new JSONObject(Map.of("type", isEnabledThink?"enabled":"disabled")));
    }

    @Override
    public double getTemperature() {
       return curReq.getDoubleValue("temperature");
    }

    @Override
    public void setTemperature(double temperature) {
        curReq.put("temperature",temperature);
    }

    @Override
    public int getTopP() {
        return curReq.getIntValue("top_p");
    }

    @Override
    public void setTopP(int topP) {
        curReq.put("top_p", topP);
    }
}
