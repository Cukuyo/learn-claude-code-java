package org.example.framework_systems.memory;

import java.util.HashMap;
import java.util.Map;

/**
 * 记忆实体：对应Python中单个md文件的记忆
 */
public class MemoryEntity {
    /**
     * 记忆类型：user/feedback/project/reference（对齐Python）
     */
    public MemoryType type;
    /**
     * 记忆唯一标识（对应Python的safe_name）
     */
    public String name;

    /**
     * 记忆描述（短摘要）
     */
    public String description;

    /**
     * 记忆内容（完整文本）
     */
    public String content;

    public MemoryEntity() {
    }

    public MemoryEntity(MemoryType type, String name, String description, String content) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.content = content;
    }

    /**
     * buildFileName
     *
     * @return FileName
     */
    public String buildFileName() {
        return type + "_" + name + ".md";
    }

    /**
     * toPrompt
     *
     * @return prompt
     */
    public String toPrompt() {
        return "- {" + type + ":" + name + ":" + System.lineSeparator()
               + description + System.lineSeparator()
               + "}";
    }

    /**
     * 转换为 md 元数据
     *
     * @return meta
     */
    public Map<String, String> toMeta() {
        Map<String, String> meta = new HashMap<>();
        meta.put("type", type.toString());
        meta.put("name", name);
        meta.put("description", description);
        return meta;
    }
}
