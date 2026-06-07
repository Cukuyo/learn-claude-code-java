package org.example.agents;

import org.example.framework_models.AbstractModel;
import org.example.framework_tool.ToolMethod;
import org.example.framework_tool.ToolParam;

import java.io.IOException;

/**
 * 父agent，包含所有功能:
 * 1、支持子agent所有功能
 * 2、支持派发子agent
 */
public class ParentAgent extends SubAgent {
    public ParentAgent(AbstractModel model, String agentName) {
        super(model, agentName);
        agent.registryTool(this);
    }

    /**
     * 分发任务到子agent
     *
     * @param content 子agent任务
     * @return 任务返回
     */
    @ToolMethod(description = "当需要执行一个复杂多步骤任务，但只需要一个结果时，使用此工具生成一个全新上下文的子智能体执行子任务，该智能体共享文件系统，但不继承会话历史")
    public String handOut(@ToolParam(description = "子任务描述") String content) {
        try {
            return new SubAgent(agent.getModel().cloneWithoutHistory(), agent.getAgentName() + "-subagent").chatOrCommand(content);
        } catch (IOException |InterruptedException e) {
            return "Error: " + e.getMessage();
        }
    }
}
