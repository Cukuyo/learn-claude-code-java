package org.example.define_systems.memory;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * 记忆实体：对应Python中单个md文件的记忆
 */
public class MemoryEntity {
    /**
     * 枚举：记忆类型
     */
    public enum MemoryType {
        USER, FEEDBACK, PROJECT, REFERENCE
    }

    /**
     * 记忆唯一标识（对应Python的safe_name）
     */
    public String name;
    /**
     * 记忆描述（短摘要）
     */
    public String description;
    /**
     * 记忆类型：user/feedback/project/reference（对齐Python）
     */
    public MemoryType type;
    /**
     * 记忆内容（完整文本）
     */
    public String content;
    /**
     * 存储路径
     */
    public Path storagePath;
    /**
     * 创建/更新时间（扩展Python逻辑，便于过期清理）
     */
    public LocalDateTime updateTime;
}
