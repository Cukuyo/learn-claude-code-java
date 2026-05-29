package org.example.agents;

import org.example.agents.extension.AgentLogPrint;
import org.example.agents.efficiency.ContextSummary;
import org.example.agents.extension.MultiStepsPlan;
import org.example.agents.efficiency.ToolUseCompact;
import org.example.agents.systems.MemorySystem;
import org.example.agents.systems.PermissionSystem;
import org.example.framework_agent.IAgent;
import org.example.framework_agent.core.AbstractAgent;
import org.example.framework_agent.core.AgentLoopAgent;
import org.example.framework_models.AbstractModel;
import org.example.utils.cmd.AgentCommandTool;
import org.example.utils.file.AgentFileTool;

import java.io.File;
import java.io.IOException;
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
     * @throws InterruptedException InterruptedException
     * @throws IOException          IOException
     */
    public static String singleChat(AbstractModel model, String agentName, String chatOrCommand) throws IOException, InterruptedException {
        return new SubAgent(model.cloneWithoutHistory(), agentName).chatOrCommand(chatOrCommand);
    }

    protected final AbstractAgent agent;

    public SubAgent(AbstractModel model, String agentName) {
        agent = new AgentLoopAgent(model, agentName);

        agent.registryTool(AgentCommandTool.class);
        agent.registryTool(AgentFileTool.class);

        agent.registrySkills(System.getProperty("user.dir") + File.separator + "skills");

        agent.registryAgentCallback(AgentLogPrint.INSTANCE);
        agent.registryAgentCallback(new MultiStepsPlan());
        agent.registryAgentCallback(new ToolUseCompact(30));
        agent.registryAgentCallback(new ContextSummary(0.5d, 3));

        PermissionSystem permissionSystem;
        try {
            permissionSystem = new PermissionSystem(Paths.get(getClass().getClassLoader().getResource("permission_deny.properties").toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        agent.registryHook(permissionSystem);
        agent.registryCommand(permissionSystem);

        MemorySystem memorySystem = new MemorySystem(Paths.get(System.getProperty("user.dir"), ".memory"));
        agent.registryAgentCallback(memorySystem);
        agent.registryCommand(memorySystem);
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
