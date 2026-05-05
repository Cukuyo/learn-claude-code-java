package org.example.agent.agent_callbacks;

import com.alibaba.fastjson2.JSONObject;
import org.example.agent.IAgent;
import org.example.agent.agent_base.AbstractAgent;
import org.example.tools.todo.TodoManager;

/**
 * agent抽象父类，提供公共方法，定义架构
 */
public class TodoManagerSupport implements AgentCallback {
    private boolean inited = false;
    private final TodoManager todoManager = new TodoManager();
    private boolean useTodo = false;

    @Override
    public void eachAtomicInitFirst(AbstractAgent agent) {
        if (!inited) {
            agent.registryTool(todoManager);
            inited = true;
        }
    }

    @Override
    public void callBeforeToolsUse(AbstractAgent agent) {
        useTodo = false;
    }

    @Override
    public void callBeforeToolUse(AbstractAgent agent, String id, String name, JSONObject arguments) {
        if (name.equals("updateTasks")) {
            useTodo = true;
        }
    }

    @Override
    public void callAfterToolsUse(AbstractAgent agent) {
        if (useTodo) {
            todoManager.noteRoundReset();
        } else {
            todoManager.noteRoundWithoutUpdate();
            String reminder = todoManager.reminder();
            if (!reminder.isEmpty()) {
                agent.getModel().addUserMessage(reminder);
            }
        }
    }
}
