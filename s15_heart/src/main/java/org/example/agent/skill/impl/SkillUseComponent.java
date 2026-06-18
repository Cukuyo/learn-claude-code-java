package org.example.agent.skill.impl;

import com.alibaba.fastjson2.JSONObject;
import org.example.agent.IAgentSkillUse;
import org.example.agent.impl.AbstractAgent;
import org.example.agent.skill.SkillManifest;
import org.example.agent.skill.SkillUtil;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * agent父类：
 * 提供skillUse实现
 */
public class SkillUseComponent implements IAgentSkillUse {
    protected AbstractAgent agent;

    protected final Map<String, SkillManifest> skillManifestMap = new HashMap<>();
    protected JSONObject skillMessage;

    public SkillUseComponent(AbstractAgent agent) {
        this.agent = agent;
        agent.registryTool(this);

        renderPrompts();
    }

    /**
     * skills注册
     *
     * @param dirPath skill目录
     */
    @Override
    public void registrySkills(Path dirPath) {
        List<SkillManifest> skillManifests = SkillUtil.resolveDir(dirPath);
        for (SkillManifest skillManifest : skillManifests) {
            skillManifestMap.put(skillManifest.name, skillManifest);
        }

        renderPrompts();
    }

    @Override
    public void removeSkills(Path dirPath) {
        List<SkillManifest> skillManifests = SkillUtil.resolveDir(dirPath);
        for (SkillManifest skillManifest : skillManifests) {
            skillManifestMap.remove(skillManifest.name);
        }

        renderPrompts();
    }

    @Override
    public void registrySkill(Path skillPath) {
        SkillManifest skillManifest = SkillUtil.resolveFile(skillPath);
        skillManifestMap.put(skillManifest.name, skillManifest);

        renderPrompts();
    }

    @Override
    public void removeSkill(Path skillPath) {
        SkillManifest skillManifest = SkillUtil.resolveFile(skillPath);
        skillManifestMap.remove(skillManifest.name);

        renderPrompts();
    }

    @Override
    public void removeSkill(String skillName) {
        skillManifestMap.remove(skillName);

        renderPrompts();
    }

    private void renderPrompts() {
        StringBuilder builder = new StringBuilder(skillManifestMap.size() * 256);
        builder.append("[SkillUse]当行动前需要特定指令时，当需要时根据路径加载技能.").append(System.lineSeparator());
        builder.append("技能如下:").append(System.lineSeparator());

        if (skillManifestMap.isEmpty()) {
            builder.append("当前无可用技能").append(System.lineSeparator());
        }

        skillManifestMap.values().forEach(cv -> builder.append(cv.toPrompt()).append(System.lineSeparator()));

        if (skillMessage != null) {
            skillMessage.put("content", builder.toString());
        } else {
            skillMessage = agent.getModel().addSystemMessages(builder.toString());
        }
    }
}
