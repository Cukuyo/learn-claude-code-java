package org.example.agent.common;

import com.alibaba.fastjson2.JSONObject;
import org.example.models.AbstractModel;
import org.example.todo.TodoManager;

/**
 * agent抽象父类，提供公共方法，定义架构
 */
public class TodoUseAgent extends ToolUseAgent {
    private final TodoManager todoManager = new TodoManager();
    private boolean useTodo = false;

    public TodoUseAgent(AbstractModel model, String agentName) {
        super(model, agentName);
    }

    @Override
    protected void callBeforeToolsUse() {
        super.callBeforeToolsUse();
        useTodo = false;
    }

    @Override
    protected void callAfterToolUse(String id, String name, JSONObject arguments, String toolRsp) {
        super.callAfterToolUse(id, name, arguments, toolRsp);
        if (name.equals("updateTasks")) {
            useTodo = true;
        }
    }

    @Override
    protected void callAfterToolsUse() {
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
