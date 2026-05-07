package org.example.define_models;

/**
 * 定义模型的参数配置
 */
public interface IModelSetting {
    String getUrl();

    String getApiKey();

    String getModel();

    int getMaxInputTokens();

    int getMaxOutTokens();
}
