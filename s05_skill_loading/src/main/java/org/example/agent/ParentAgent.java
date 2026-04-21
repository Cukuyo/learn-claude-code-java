package org.example.agent;

import com.alibaba.fastjson2.JSONObject;
import org.example.models.AbstractModel;
import org.example.todo.TodoManager;
import org.example.tool.ToolMethod;
import org.example.tool.ToolParam;

import java.io.IOException;

/**
 * 父agent，包含所有功能:
 * 1、支持子agent所有功能
 * 2、支持todoManager
 */
public class ParentAgent extends SubAgent {
    private final TodoManager todoManager = new TodoManager();
    private boolean useTodo = false;

    public ParentAgent(AbstractModel model, String agentName) {
        super(model, agentName);
        registryTool(SubAgent.class);
        registryTool(todoManager);
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

    @Override
    public void callBeforeToolsUse() {
        super.callBeforeToolsUse();
        useTodo = false;
    }

    @Override
    public void callAfterToolUse(String id, String name, JSONObject arguments, String toolRsp) {
        super.callAfterToolUse(id, name, arguments, toolRsp);
        if (name.equals("updateTasks")) {
            useTodo = true;
        }
    }

    @Override
    public void callAfterToolsUse() {
        super.callAfterToolsUse();
        if (useTodo) {
            todoManager.noteRoundReset();
        } else {
            todoManager.noteRoundWithoutUpdate();
            String reminder = todoManager.reminder();
            if (!reminder.isEmpty()) {
                model.addUserMessage(reminder);
            }
        }
    }
}
