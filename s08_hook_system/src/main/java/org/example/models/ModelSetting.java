package org.example.models;

/**
 * 定义模型的参数配置
 */
public interface ModelSetting {
    String getUrl();

    String getApiKey();

    String getModel();

    int getMaxInputTokens();

    int getMaxOutTokens();
}
