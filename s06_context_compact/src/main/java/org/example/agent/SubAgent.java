package org.example.agent;

import org.example.agent.callbacks.AgentCallback;
import org.example.agent.callbacks.AgentLogPrintSupport;
import org.example.agent.callbacks.TodoManagerSupprt;
import org.example.agent.base.IAgent;
import org.example.agent.base.SkillUseAgent;
import org.example.models.AbstractModel;
import org.example.utils.AgentFileUtil;
import org.example.utils.CommandUtil;

import java.io.IOException;

/**
 * 子agent，只有基础功能:
 * 1、支持命令行工具
 * 2、支持文件编辑工具
 */
public class SubAgent implements IAgent {
    protected final IAgent agent;

    public SubAgent(AbstractModel model, String agentName) {
        agent = new SkillUseAgent(model, agentName);
        agent.registryTool(CommandUtil.class);
        agent.registryTool(AgentFileUtil.class);

        agent.registryAgentCallback(AgentLogPrintSupport.INSTANCE);
        agent.registryAgentCallback(new TodoManagerSupprt());
    }

    @Override
    public AbstractModel getModel() {
        return agent.getModel();
    }

    @Override
    public String getAgentName() {
        return agent.getAgentName();
    }

    @Override
    public String chatOrCommand(String content) throws IOException, InterruptedException {
        return agent.chatOrCommand(content);
    }

    @Override
    public void registryTool(Object toolObj) {
        agent.registryTool(toolObj);
    }

    @Override
    public void registryTool(Class<?> toolObj) {
        agent.registryTool(toolObj);
    }

    @Override
    public void registrySkills(String dirPath) {
        agent.registrySkills(dirPath);
    }

    @Override
    public void registryAgentCallback(AgentCallback agentCallback) {
        agent.registryAgentCallback(agentCallback);
    }
}
