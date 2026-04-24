package org.example.agent.callbacks;

import org.example.agent.base.IAgent;

/**
 * agent抽象父类，提供公共方法，定义架构
 */
public class ContextCompactAgent implements AgentCallback {
    @Override
    public void callBeforeChat(IAgent agent) {
        AgentCallback.super.callBeforeChat(agent);
    }

    @Override
    public void callBeforeToolsUse(IAgent agent) {
        AgentCallback.super.callBeforeToolsUse(agent);
    }
}
