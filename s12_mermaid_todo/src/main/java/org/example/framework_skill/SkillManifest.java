package org.example.framework_skill;

import java.nio.file.Path;

/**
 * skill 元数据
 *
 * @param name        name
 * @param description 描述
 * @param dirPath     所在目录路径
 */
public class SkillManifest {
    public String name;
    public String description;
    public Path dirPath;

    public SkillManifest(String name, String description, Path dirPath) {
        this.name = name;
        this.description = description;
        this.dirPath = dirPath;
    }
}
