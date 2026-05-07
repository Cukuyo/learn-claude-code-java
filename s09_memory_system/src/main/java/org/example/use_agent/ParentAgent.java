package org.example.use_agent;

import org.example.models.AbstractModel;
import org.example.use_tool.ToolMethod;
import org.example.use_tool.ToolParam;

import java.io.IOException;

/**
 * 父agent，包含所有功能:
 * 1、支持子agent所有功能
 * 2、支持派发子agent
 */
public class ParentAgent extends SubAgent {
    public ParentAgent(AbstractModel model, String agentName) throws IOException {
        super(model, agentName);
        agent.registryTool(this);
    }

    /**
     * 分发任务到子agent
     *
     * @param content 子agent任务
     * @return 任务返回
     * @throws IOException          io异常
     * @throws InterruptedException 等待中断
     */
    @ToolMethod(description = "生成一个全新上下文的子智能体执行子任务，该智能体共享文件系统，但不继承会话历史")
    public String handOut(@ToolParam(description = "子任务描述") String content) throws IOException, InterruptedException {
        return new SubAgent(agent.getModel().cloneWithoutHistory(), agent.getAgentName() + "-subagent").chatOrCommand(content);
    }
}
