package org.example.s01_agent_loop.models;

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
    String getUrl() {
        return url;
    }

    @Override
    String getApiKey() {
        return apiKey;
    }
}
