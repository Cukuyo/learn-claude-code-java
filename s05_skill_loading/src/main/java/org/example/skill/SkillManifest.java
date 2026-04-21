package org.example.skill;

import java.nio.file.Path;

/**
 * skill 元数据
 *
 * @param name        name
 * @param description 描述
 * @param dirPath     所在目录路径
 */
public record SkillManifest(String name, String description, Path dirPath) {

}
