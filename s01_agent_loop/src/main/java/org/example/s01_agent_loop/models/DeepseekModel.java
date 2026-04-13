package org.example.s01_agent_loop.models;

import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;

/**
 * Deepseek客户端
 */
public class DeepseekModel extends AbstractModel {
    private final String url;
    private final String apiKey;

    public DeepseekModel(String apiKey) {
        this("https://api.deepseek.com/chat/completions", apiKey);
    }

    public DeepseekModel(String url, String apiKey) {
        this("deepseek-chat", url, apiKey);
    }

    public DeepseekModel(String model, String url, String apiKey) {
        super(model);
        this.url = url;
        this.apiKey = "Bearer " + apiKey;
    }

    @Override
    public JSONObject chat() throws IOException, InterruptedException {
        return super.chat().getJSONArray("choices").getJSONObject(0);
    }

    @Override
    String getUrl() {
        return url;
    }

    @Override
    String getApiKey() {
        return apiKey;
    }
}
