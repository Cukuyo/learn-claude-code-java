package org.example.framework_systems.task;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * task 实体
 */
public class TaskEntity {
    /**
     * agent名称
     */
    public String agent;
    /**
     * 任务名称
     */
    public String name;
    /**
     * 任务描述（短摘要）
     */
    public String description;
    /**
     * 任务进度
     */
    public int progress;
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

    /**
     * toPrompt
     *
     * @return prompt
     */
    public String toPrompt() {
        return "- {" + name + ":" + description + ":" + System.lineSeparator()
                + content + System.lineSeparator()
                + "}";
    }

    /**
     * 转换为 md 元数据
     *
     * @return meta
     */
    public Map<String, String> toMeta() {
        Map<String, String> meta = new HashMap<>();
        meta.put("agent", agent);
        meta.put("name", name);
        meta.put("description", description);
        meta.put("progress", String.valueOf(progress));
        return meta;
    }
}
