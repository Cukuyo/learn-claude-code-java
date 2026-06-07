package org.example.framework_skill;

import java.nio.file.Path;

/**
 * skill 元数据
 */
public class SkillManifest {
    public String name;
    public String description;
    public Path filePath;

    public SkillManifest(String name, String description, Path dirPath) {
        this.name = name;
        this.description = description;
        this.filePath = dirPath;
    }

    /**
     * toPrompt
     *
     * @return Prompt
     */
    public String toPrompt() {
        return "- {" + name + ":" + description + ":" + filePath + "}";
    }
}
