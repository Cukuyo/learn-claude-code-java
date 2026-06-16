package org.example.models;

import com.alibaba.fastjson2.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * model抽象父类，提供openAi、anthropic的抽象公共部分
 */
public abstract class AbstractModel implements IModel, IModelSetting, IModelToolUse, IModelClone<AbstractModel>, IModelMonitor {
    public JSONObject curReq = new JSONObject();
    protected Set<String> toolsSet = new HashSet<>();

    protected String url;
    protected String apiKey;
    protected String model;

    protected long lastPromptTokens = 0;
    protected long lastCompletionTokens = 0;
    protected long lastTotalTokens = 0;

    protected long promptTokensSum = 0;
    protected long completionTokensSum = 0;
    protected long totalTokensSum = 0;

    public AbstractModel(String url, String apiKey, String model) {
        this.url = url;
        this.apiKey = apiKey;
        this.model = model;
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
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void setModel(String model) {
        this.model = model;
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
    public void addTool(JSONObject tool) {
        String toolName = extractToolName(tool);
        if (!toolsSet.contains(toolName)) {
            getTools().add(tool);
            toolsSet.add(toolName);
        }
    }
}
