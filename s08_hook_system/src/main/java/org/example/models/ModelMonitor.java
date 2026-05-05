package org.example.models;

/**
 * 模型使用监控
 */
public interface ModelMonitor {
    long getLastChatTotalTokens();

    long getLastChatPromptTokens();

    long getLastChatCompletionTokens();

    long getTotalTokensSum();

    long getPromptTokensSum();

    long getCompletionTokensSum();
}
