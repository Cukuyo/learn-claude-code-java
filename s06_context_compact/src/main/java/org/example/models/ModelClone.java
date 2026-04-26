package org.example.models;

/**
 * 定义模型的克隆
 */
public interface ModelClone<T> {
    /**
     * 带全部历史信息的克隆
     */
    T cloneWithHistory(T newT);

    /**
     * 仅带系统提示词的克隆
     */
    T cloneWithoutHistory(T newT);

    /**
     * 带全部历史信息的克隆
     */
    T cloneWithHistory();

    /**
     * 仅带系统提示词的克隆
     */
    T cloneWithoutHistory();
}
