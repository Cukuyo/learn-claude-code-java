package org.example.framework_systems.memory;

import org.example.framework_systems.BaseEntity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 记忆实体：对应Python中单个md文件的记忆
 */
public class MemoryEntity extends BaseEntity {
    /**
     * 记忆类型：user/feedback/project/reference（对齐Python）
     */
    public MemoryType type;

    /**
     * buildFileName
     *
     * @return FileName
     */
    @Override
    public String buildFileName() {
        return type + File.separator + name + ".md";
    }

    /**
     * toPrompt
     *
     * @return prompt
     */
    @Override
    public String toPrompt() {
        return "- {" + type + ":" + name + ":" + filePath + ":" + description + "}";
    }

    /**
     * 转换为 md 元数据
     *
     * @return meta
     */
    @Override
    public Map<String, String> toMeta() {
        Map<String, String> meta = new HashMap<>();
        meta.put("type", type.toString());
        meta.put("name", name);
        meta.put("description", description);
        return meta;
    }
}
