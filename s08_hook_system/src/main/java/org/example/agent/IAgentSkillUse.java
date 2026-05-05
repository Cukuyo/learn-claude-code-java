package org.example.agent;

/**
 * agent应支持注册skill
 */
public interface IAgentSkillUse {
    /**
     * skill注册
     *
     * @param dirPath skill.md所在目录
     */
    void registrySkills(String dirPath);
}
