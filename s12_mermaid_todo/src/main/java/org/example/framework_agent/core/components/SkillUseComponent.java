package org.example.framework_agent.core.components;

import com.alibaba.fastjson2.JSONObject;
import org.example.framework_agent.IAgentSkillUse;
import org.example.framework_agent.core.AbstractAgent;
import org.example.framework_skill.SkillDirUtil;
import org.example.framework_skill.SkillManifest;

import java.nio.file.Paths;
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
    public void registrySkills(String dirPath) {
        List<SkillManifest> skillManifests = SkillDirUtil.resolveDir(Paths.get(dirPath));
        for (SkillManifest skillManifest : skillManifests) {
            skillManifestMap.put(skillManifest.name, skillManifest);
        }

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
            skillMessage = agent.model.addSystemMessages(builder.toString());
        }
    }
}
