package org.example.framework_systems;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 基础实体
 */
public abstract class BaseEntity {
    public String name;
    public String description;
    public String content;
    public Path filePath;
    public LocalDateTime updateTime;

    public abstract String buildFileName();

    public abstract String toPrompt();

    public abstract Map<String, String> toMeta();
}
