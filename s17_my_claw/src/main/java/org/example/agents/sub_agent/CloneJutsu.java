package org.example.agents.sub_agent;

import org.example.agent.AgentCallback;
import org.example.agent.impl.AbstractAgent;
import org.example.agent.tool.ToolExecuter;
import org.example.agent.tool.ToolMethod;
import org.example.agent.tool.ToolParam;
import org.example.agents.MyAgent;
import org.example.models.AbstractModel;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 分身术
 */
public class CloneJutsu implements AgentCallback {
    private static final ExecutorService EXECUTOR = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("CloneJutsu", 0).factory());
    private AbstractAgent agent = null;

    @Override
    public void initSelf(AbstractAgent agent) {
        this.agent = agent;
        agent.registryTool(this);
    }

    @Override
    public void removeSelf(AbstractAgent agent) {
        agent.removeTool(this);
    }

    /**
     * 分身术
     *
     * @param jutsuName jutsuName
     * @param content   content
     * @return 执行结果
     */
    @ToolMethod(description = "[分身术]用于执行一个复杂多步骤长下文但只需要一个结果的任务时，为减少上下文消耗，使用此工具生成不含历史记忆的分身执行子任务，")
    public Future<String> cloneJutsu(
            @ToolParam(description = "子任务名称，驼峰加下划线的形式") String jutsuName,
            @ToolParam(description = "子任务描述，包含必要上下文、任务描述、完成验证标准") String content,
            @ToolParam(description = "是否要异步执行，对于耗时较长的命令可以多个命令异步执行以提高效率") boolean isAsync) {
        if (isAsync) {
            return EXECUTOR.submit(() -> cloneJutsuSync(jutsuName, content));
        } else {
            return ToolExecuter.simpleRsp(cloneJutsuSync(jutsuName, content));
        }
    }

    private String cloneJutsuSync(String jutsuName, String content) {
        if (agent.getAgentName().contains("-subagent")) {
            return "分身不能使用！";
        }
        try {
            AbstractModel model = agent.getModel().cloneNewModel();
            String agentName = agent.getAgentName() + "-subagent-" + jutsuName;
            String agentRole = agent.getAgentRole() + ";" + "你是" + agent.getAgentName() + "的分身，用于帮助宿主执行一个复杂多步骤长下文但只需要一个结果的任务时，减少上下文消耗";
            return new MyAgent(model, agentName, agentRole).chatOrCommand(content);
        } catch (IOException | InterruptedException e) {
            return "Error: " + e.getMessage();
        }
    }
}
