package org.example.agent.base;

import com.alibaba.fastjson2.JSONObject;
import org.example.models.AbstractModel;
import org.example.skill.SkillManifest;
import org.example.skill.SkillReadUtil;
import org.example.skill.SkillResolvUtil;
import org.example.tool.ToolMethod;
import org.example.tool.ToolParam;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * agent父类：
 * 提供skillUse实现
 */
public class SkillUseAgent extends ToolUseAgent {
    protected final Map<String, SkillManifest> skillManifestMap = new HashMap<>();
    protected JSONObject skillMessage;

    public SkillUseAgent(AbstractModel model, String agentName) {
        super(model, agentName);

        registryTool(this);
    }

    /**
     * skills注册
     *
     * @param dirPath skill目录
     */
    public void registrySkills(String dirPath) {
        List<SkillManifest> skillManifests = SkillResolvUtil.resolveDir(Paths.get(dirPath));
        for (SkillManifest skillManifest : skillManifests) {
            skillManifestMap.put(skillManifest.name(), skillManifest);
        }

        if (skillMessage != null) {
            skillMessage.put("content", renderSkills());
        } else {
            skillMessage = model.addSystemMessages(renderSkills());
        }
    }

    private String renderSkills() {
        StringBuilder builder = new StringBuilder(skillManifestMap.size() * 128);
        builder.append("当行动前需要特定指令时，使用<loadSkill>工具加载技能.").append(System.lineSeparator());
        builder.append("技能如下:").append(System.lineSeparator());

        if (skillManifestMap.isEmpty()) {
            builder.append("当前无可用技能").append(System.lineSeparator());
        }

        for (SkillManifest skillManifest : skillManifestMap.values()) {
            builder.append("- {").append(skillManifest.name()).append(":").append(skillManifest.description()).append(":").append("所在目录路径为").append(skillManifest.dirPath().relativize(Paths.get(System.getProperty("user.dir")))).append("}").append(System.lineSeparator());
        }
        return builder.toString();
    }

    @ToolMethod(description = "本function用于根据指定的skill名称，将SKILL.md全部内容加载到当前会话")
    public String loadSkill(@ToolParam(description = "指定的skill名称") String skillName) throws IOException {
        return SkillReadUtil.readSkillMDBody(skillManifestMap.get(skillName).dirPath());
    }
}
