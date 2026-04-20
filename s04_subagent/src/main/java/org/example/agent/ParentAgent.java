package org.example.agent;

import com.alibaba.fastjson2.JSONObject;
import org.example.models.AbstractModel;
import org.example.todo.TodoManager;

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
        registryTool(todoManager);
    }

    @Override
    public void callBeforeToolsUse() {
        super.callBeforeToolsUse();
        useTodo = false;
    }

    @Override
    public void callAfterToolUse(String id, String name, JSONObject arguments) {
        super.callAfterToolUse(id, name, arguments);
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
