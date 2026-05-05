package org.example.use_agent;

import org.example.agent.IAgent;
import org.example.agent.agent_base.AbstractAgent;
import org.example.agent.agent_base.SkillUseAgent;
import org.example.agent.agent_callbacks.AgentLogPrintSupport;
import org.example.agent.agent_callbacks.ContextSummarySupport;
import org.example.agent.agent_callbacks.TodoManagerSupport;
import org.example.agent.agent_callbacks.ToolUseCompactSupport;
import org.example.models.AbstractModel;
import org.example.tools.cmd.AgentCommandTool;
import org.example.tools.file.AgentFileTool;

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
    protected final AbstractAgent agent;

    public SubAgent(AbstractModel model, String agentName) {
        agent = new SkillUseAgent(model, agentName);

        agent.registryTool(AgentCommandTool.class);
        agent.registryTool(AgentFileTool.class);

        agent.registrySkills(System.getProperty("user.dir") + File.separator + "skills");

        agent.registryAgentCallback(AgentLogPrintSupport.INSTANCE);
        agent.registryAgentCallback(new TodoManagerSupport());
        agent.registryAgentCallback(new ToolUseCompactSupport(10));
        agent.registryAgentCallback(new ContextSummarySupport(0.5d, 3));

        agent.registryHook();
        agent.registryCommand();
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
}
