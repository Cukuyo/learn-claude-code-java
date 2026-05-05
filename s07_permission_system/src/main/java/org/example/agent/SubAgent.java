package org.example.agent;

import org.example.agent.base.SkillUseAgent;
import org.example.agent.callbacks.*;
import org.example.models.AbstractModel;
import org.example.tools.file.AgentFileTool;
import org.example.tools.cmd.AgentCommandTool;

import java.io.File;
import java.io.IOException;

/**
 * 子agent，封装了SkillUseAgent，添加了:
 * 1、支持命令行工具
 * 2、支持文件编辑工具
 * 3、支持skills
 * <p>
 * 4、支持todoManager
 * 5、支持上下文压缩
 */
public class SubAgent implements IAgent {
    protected final IAgent agent;

    public SubAgent(AbstractModel model, String agentName) {
        agent = new SkillUseAgent(model, agentName);

        registryTool(AgentCommandTool.class);
        registryTool(AgentFileTool.class);
        registrySkills(System.getProperty("user.dir") + File.separator + "skills");

        registryAgentCallback(AgentLogPrintSupport.INSTANCE);
        registryAgentCallback(new TodoManagerSupport());
        registryAgentCallback(new ToolUseCompactSupport(10));
        registryAgentCallback(new ContextSummarySupport(0.5d, 3));
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
