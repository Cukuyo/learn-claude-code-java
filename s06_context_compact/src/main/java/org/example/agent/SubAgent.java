package org.example.agent;

import org.example.agent.common.SkillUseAgent;
import org.example.agent.common.ToolUseAgent;
import org.example.models.AbstractModel;
import org.example.utils.AgentFileUtil;
import org.example.utils.CommandUtil;

/**
 * 子agent，只有基础功能:
 * 1、支持命令行工具
 * 2、支持文件编辑工具
 */
public class SubAgent extends SkillUseAgent {
    public SubAgent(AbstractModel model, String agentName) {
        super(model, agentName);
        registryTool(CommandUtil.class);
        registryTool(AgentFileUtil.class);
    }
}
