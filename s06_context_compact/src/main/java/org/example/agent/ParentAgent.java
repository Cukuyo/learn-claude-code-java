package org.example.agent;

import org.example.models.AbstractModel;
import org.example.tool.ToolMethod;
import org.example.tool.ToolParam;

import java.io.IOException;

/**
 * 父agent，包含所有功能:
 * 1、支持子agent所有功能
 * 2、支持派发子agent
 */
public class ParentAgent extends SubAgent {
    public ParentAgent(AbstractModel model, String agentName) {
        super(model, agentName);
        registryTool(this);
    }

    /**
     * 分发任务到子agent
     *
     * @param content 子agent任务
     * @return 任务返回
     * @throws IOException          io异常
     * @throws InterruptedException 等待中断
     */
    @ToolMethod(description = "Spawn a subagent with fresh context. It shares the filesystem but not conversation history.")
    public String handOut(@ToolParam(description = "Short description of the task") String content) throws IOException, InterruptedException {
        return new SubAgent(model.cloneWithSystemMessages(), agentName + "-subagent").chatOrCommand(content);
    }
}
