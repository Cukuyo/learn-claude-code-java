package org.example.framework_models;

/**
 * 模型使用监控
 */
public interface IModelMonitor {
    long getLastChatTotalTokens();

    long getLastChatPromptTokens();

    long getLastChatCompletionTokens();

    long getTotalTokensSum();

    long getPromptTokensSum();

    long getCompletionTokensSum();
}
