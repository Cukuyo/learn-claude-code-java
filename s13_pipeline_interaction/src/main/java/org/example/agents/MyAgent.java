package org.example.agents;

import org.example.agents.context.ContextSummary;
import org.example.agents.context.ToolUseCompact;
import org.example.agents.log.AgentLogPrint;
import org.example.agents.persist.MemorySystem;
import org.example.agents.persist.PermissionSystem;
import org.example.agents.persist.TaskSystem;
import org.example.agent.IAgent;
import org.example.agent.impl.AbstractAgent;
import org.example.agent.impl.AgentLoopAgent;
import org.example.framework_models.AbstractModel;
import org.example.agent.tool.ToolMethod;
import org.example.agent.tool.ToolParam;
import org.example.utils.cmd.AgentCommandTool;
import org.example.utils.file.AgentFileTool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * 子agent，添加默认能力
 */
public final class MyAgent implements IAgent {
    private static final PermissionSystem PERMISSION_SYSTEM;
    private static final MemorySystem MEMORY_SYSTEM;
    private static final TaskSystem TASK_SYSTEM;
    private static final ListCommand LIST_COMMAND = new ListCommand();

    static {
        try {
            PERMISSION_SYSTEM = new PermissionSystem(Paths.get(MyAgent.class.getClassLoader().getResource("permission_deny.properties").toURI()));
            MEMORY_SYSTEM = new MemorySystem(Paths.get(System.getProperty("user.dir"), ".memories"));
            TASK_SYSTEM = new TaskSystem(Paths.get(System.getProperty("user.dir"), ".tasks"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final AbstractAgent agent;

    public MyAgent(AbstractModel model, String agentName, String agentRole) {
        agent = new AgentLoopAgent(model, agentName, agentRole);
        agent.registryTool(this);

        agent.registryTool(AgentCommandTool.class);
        agent.registryTool(AgentFileTool.class);

        agent.registrySkills(System.getProperty("user.dir") + File.separator + "skills");

        agent.registryAgentCallback(AgentLogPrint.INSTANCE);
        agent.registryAgentCallback(new ToolUseCompact(30, 50));
        agent.registryAgentCallback(new ContextSummary(0.5d, 3));

        agent.registryHook(PERMISSION_SYSTEM);
        agent.registryCommand(PERMISSION_SYSTEM);

        agent.registryAgentCallback(MEMORY_SYSTEM);
        agent.registryCommand(MEMORY_SYSTEM);

        agent.registryAgentCallback(TASK_SYSTEM);

        agent.registryCommand(LIST_COMMAND);
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
    public String getAgentRole() {
        return agent.getAgentRole();
    }

    @Override
    public String chatOrCommand(String content) throws IOException, InterruptedException {
        return agent.chatOrCommand(content);
    }

    @Override
    public String command(String command) {
        return agent.command(command);
    }

    @Override
    public String command(String name, String command) {
        return agent.command(name, command);
    }

    @Override
    public String chat(String chatContent) throws IOException, InterruptedException {
        return agent.chat(chatContent);
    }

    @Override
    public String chat(List<String> nameList, List<String> chatContentList) throws IOException, InterruptedException {
        return agent.chat(nameList, chatContentList);
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
            return new MyAgent(model, agentName, agent.getAgentRole()).chatOrCommand(content);
        } catch (IOException | InterruptedException e) {
            return "Error: " + e.getMessage();
        }
    }
}
