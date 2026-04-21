package org.example.agent;

import org.example.models.AbstractModel;
import org.example.utils.AgentFileUtil;
import org.example.utils.CommandUtil;

import java.io.File;

/**
 * 子agent，只有基础功能:
 * 1、支持基本工具
 */
public class SubAgent extends AbstractAgent {
    public SubAgent(AbstractModel model, String agentName) {
        super(model, agentName);
        registryTool(CommandUtil.class);
        registryTool(AgentFileUtil.class);
        registryTool(this);
        registrySkills(System.getProperty("user.dir") + File.separator + "skills");
    }
}
