package org.example.framework_models;

import org.example.utils.JsonCloneUtil;

/**
 * Deepseek客户端
 */
public class DeepseekModel extends AbstractOpenAiModel {
    private static final int MAX_INPUT_TOKENS = 100 * 10000;
    private static final int MAX_OUTPUT_TOKENS = 384 * 1000;

    public DeepseekModel(String apiKey) {
        this("https://api.deepseek.com/chat/completions", apiKey);
    }

    public DeepseekModel(String url, String apiKey) {
        this("deepseek-v4-flash", url, apiKey);
    }

    public DeepseekModel(String model, String url, String apiKey) {
        super(url, apiKey, model);
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
        DeepseekModel deepseekModel = new DeepseekModel(model, url, apiKey);
        deepseekModel.curReq = JsonCloneUtil.deepClone(curReq);
        deepseekModel.getTools().clear();
        return deepseekModel;
    }

    @Override
    public AbstractModel cloneWithoutHistory() {
        DeepseekModel deepseekModel = new DeepseekModel(model, url, apiKey);
        deepseekModel.curReq = JsonCloneUtil.deepClone(curReq);
        deepseekModel.getMessages().clear();
        deepseekModel.getTools().clear();
        return deepseekModel;
    }
}
