package org.example.framework_agent.core.components;

import com.alibaba.fastjson2.JSONObject;

import org.example.framework_agent.IAgentSkillUse;
import org.example.framework_agent.core.AbstractAgent;
import org.example.framework_skill.SkillManifest;
import org.example.framework_skill.SkillFileUtil;
import org.example.framework_skill.SkillDirUtil;
import org.example.framework_tool.ToolMethod;
import org.example.framework_tool.ToolParam;

import java.io.IOException;
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
            skillManifestMap.put(skillManifest.name(), skillManifest);
        }

        renderPrompts();
    }

    private void renderPrompts() {
        StringBuilder builder = new StringBuilder(skillManifestMap.size() * 128);
        builder.append("[SkillUse]当行动前需要特定指令时，使用<loadSkill>工具加载技能.").append(System.lineSeparator());
        builder.append("技能如下:").append(System.lineSeparator());

        if (skillManifestMap.isEmpty()) {
            builder.append("当前无可用技能").append(System.lineSeparator());
        }

        for (SkillManifest skillManifest : skillManifestMap.values()) {
            builder.append("- {")
                    .append(skillManifest.name())
                    .append(":").append(skillManifest.description())
                    .append(":").append("所在目录相对路径为").append(Paths.get(System.getProperty("user.dir")).relativize(skillManifest.dirPath()))
                    .append("}").append(System.lineSeparator());
        }

        if (skillMessage != null) {
            skillMessage.put("content", builder.toString());
        } else {
            skillMessage = agent.model.addSystemMessages(builder.toString());
        }
    }

    @ToolMethod(description = "用于根据指定的skill名称，将SKILL.md全部内容加载到当前会话")
    public String loadSkill(@ToolParam(description = "指定的skill名称") String skillName) {
        try {
            return SkillFileUtil.readSkillMDBody(skillManifestMap.get(skillName).dirPath());
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
