package org.example.agents;

import org.example.agents.efficiency.ContextSummary;
import org.example.agents.efficiency.ToolUseCompact;
import org.example.agents.extension.AgentLogPrint;
import org.example.agents.systems.MemorySystem;
import org.example.agents.security.PermissionSystem;
import org.example.agents.systems.TaskSystem;
import org.example.framework_agent.IAgent;
import org.example.framework_agent.core.AbstractAgent;
import org.example.framework_agent.core.AgentLoopAgent;
import org.example.framework_models.AbstractModel;
import org.example.framework_tool.ToolMethod;
import org.example.framework_tool.ToolParam;
import org.example.utils.cmd.AgentCommandTool;
import org.example.utils.file.AgentFileTool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 子agent，添加默认能力
 */
public final class MyAgent implements IAgent {
    private static final PermissionSystem permissionSystem;
    private static final MemorySystem memorySystem;
    private static final TaskSystem taskSystem;

    static {
        try {
            permissionSystem = new PermissionSystem(Paths.get(MyAgent.class.getClassLoader().getResource("permission_deny.properties").toURI()));
            memorySystem = new MemorySystem(Paths.get(System.getProperty("user.dir"), ".memories"));
            taskSystem = new TaskSystem(Paths.get(System.getProperty("user.dir"), ".tasks"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final AbstractAgent agent;

    public MyAgent(AbstractModel model, String agentName) {
        agent = new AgentLoopAgent(model, agentName);
        agent.registryTool(this);

        agent.registryTool(AgentCommandTool.class);
        agent.registryTool(AgentFileTool.class);

        agent.registrySkills(System.getProperty("user.dir") + File.separator + "skills");

        agent.registryAgentCallback(AgentLogPrint.INSTANCE);
        agent.registryAgentCallback(new ToolUseCompact(30, 50));
        agent.registryAgentCallback(new ContextSummary(0.5d, 3));

        agent.registryHook(permissionSystem);
        agent.registryCommand(permissionSystem);

        agent.registryAgentCallback(memorySystem);
        agent.registryCommand(memorySystem);

        agent.registryAgentCallback(taskSystem);
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

    /**
     * 分身术
     *
     * @param jutsuName jutsuName
     * @param content   content
     * @return 执行结果
     */
    @ToolMethod(description = "[分身术]用于执行一个复杂多步骤长下文但只需要一个结果的任务时，为减少上下文消耗，使用此工具生成不含历史记忆的分身执行子任务，")
    public String cloneJutsu(
            @ToolParam(description = "子任务名称，驼峰加下划线的形式") String jutsuName,
            @ToolParam(description = "子任务描述，包含必要上下文、任务描述、完成验证标准") String content) {
        if (agent.getAgentName().contains("-subagent")) {
            return "分身不能使用！";
        }
        try {
            String agentName = agent.getAgentName() + "-subagent" + jutsuName;
            AbstractModel model = agent.getModel().cloneWithoutHistory();
            return new MyAgent(model, agentName).chatOrCommand(content);
        } catch (IOException | InterruptedException e) {
            return "Error: " + e.getMessage();
        }
    }
}
