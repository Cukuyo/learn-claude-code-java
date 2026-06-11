package org.example.agents.persist.systems.task;

import org.example.agents.persist.systems.BaseEntity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * task 实体
 */
public class TaskEntity extends BaseEntity {
    /**
     * 归属于哪个agent
     */
    public String agentName;

    /**
     * 任务进度
     */
    public int progress;

    /**
     * buildFileName
     *
     * @return FileName
     */
    @Override
    public String buildFileName() {
        return agentName + File.separator + name + ".md";
    }

    /**
     * toPrompt
     *
     * @return prompt
     */
    @Override
    public String toPrompt() {
        return "- {" + name + ":" + description + ":" + filePath + ":" + content + "}";
    }

    /**
     * 转换为 md 元数据
     *
     * @return meta
     */
    @Override
    public Map<String, String> toMeta() {
        Map<String, String> meta = new HashMap<>();
        meta.put("agentName", agentName);
        meta.put("name", name);
        meta.put("description", description);
        meta.put("progress", String.valueOf(progress));
        return meta;
    }
}
