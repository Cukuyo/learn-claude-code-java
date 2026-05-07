package org.example.use_agents;

import org.example.define_agent.IAgent;
import org.example.define_agent.core.AbstractAgent;
import org.example.define_agent.core.AgentLoopAgent;
import org.example.define_agent.core.components.SkillUseComponent;
import org.example.define_models.AbstractModel;
import org.example.use_agents.extra.AgentLogPrint;
import org.example.use_agents.extra.ContextSummary;
import org.example.use_agents.extra.PermissionSystem;
import org.example.use_agents.extra.MultiStepsPlan;
import org.example.use_agents.extra.ToolUseCompact;
import org.example.use_tools.cmd.AgentCommandTool;
import org.example.use_tools.file.AgentFileTool;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * 子agent，添加默认能力
 */
public class SubAgent implements IAgent {
    /**
     * 创建一个单独的子agent执行任务
     *
     * @param model         模型
     * @param agentName     agentName
     * @param chatOrCommand 提示词
     * @return llm 返回
     */
    public static String singleChat(AbstractModel model, String agentName, String chatOrCommand) throws IOException, InterruptedException {
        return new SubAgent(model.cloneWithoutHistory(), agentName).chatOrCommand(chatOrCommand);
    }

    protected final AbstractAgent agent;

    public SubAgent(AbstractModel model, String agentName) throws IOException {
        agent = new AgentLoopAgent(model, agentName);

        agent.registryTool(AgentCommandTool.class);
        agent.registryTool(AgentFileTool.class);

        agent.registrySkills(System.getProperty("user.dir") + File.separator + "skills");

        agent.registryAgentCallback(AgentLogPrint.INSTANCE);
        agent.registryAgentCallback(new MultiStepsPlan());
        agent.registryAgentCallback(new ToolUseCompact(10));
        agent.registryAgentCallback(new ContextSummary(0.5d, 3));

        PermissionSystem permissionSystem;
        try {
            permissionSystem = new PermissionSystem(Paths.get(getClass().getClassLoader().getResource("permission_deny.properties").toURI()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        agent.registryHook(permissionSystem);
        agent.registryCommand(permissionSystem);
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
