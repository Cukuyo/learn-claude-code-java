package org.example.define_systems.memory;

import java.io.IOException;

/**
 * 记忆管理器核心接口：封装记忆的CRUD，对齐Python的MemoryManager
 */
public interface IMemoryManager {
    /**
     * 加载所有持久化记忆（会话启动时调用）
     */
    void loadAllMemories() throws IOException;

    /**
     * 保存记忆（对齐Python的save_memory）
     *
     * @param memory memory
     * @return 保存结果
     */
    String saveMemory(MemoryEntity memory) throws IOException;

    /**
     * 构建注入到System Prompt的记忆文本（对齐Python的load_memory_prompt）
     *
     * @return 记忆提示词
     */
    //
    String buildMemoryPrompt();

    /**
     * 根据名称查询记忆
     *
     * @param name name
     * @return 记忆
     */
    MemoryEntity getMemory(String name);

    /**
     * 清理过期记忆（扩展Python的DreamConsolidator）
     */
    void pruneExpiredMemories();
}
