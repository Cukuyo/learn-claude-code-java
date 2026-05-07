package org.example.use_agents.extra;

import com.alibaba.fastjson2.JSONObject;

import org.example.define_agent.AgentCallback;
import org.example.define_agent.core.AbstractAgent;
import org.example.use_tools.todo.TodoManager;

/**
 * 多步骤规划
 */
public class MultiStepsPlan implements AgentCallback {
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
