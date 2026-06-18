package org.example.agent;

import java.nio.file.Path;

/**
 * agent应支持注册skill
 */
public interface IAgentSkillUse {
    /**
     * skills注册
     *
     * @param dirPath skill.md所在目录
     */
    void registrySkills(Path dirPath);

    /**
     * skills移除
     *
     * @param dirPath skill.md所在目录
     */
    void removeSkills(Path dirPath);

    /**
     * skill注册
     *
     * @param skillPath skill.md所在目录
     */
    void registrySkill(Path skillPath);

    /**
     * skill移除
     *
     * @param skillPath skill.md所在目录
     */
    void removeSkill(Path skillPath);

    /**
     * skill移除
     *
     * @param skillName skill name
     */
    void removeSkill(String skillName);
}
