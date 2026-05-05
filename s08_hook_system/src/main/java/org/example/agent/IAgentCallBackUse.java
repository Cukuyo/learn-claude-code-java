package org.example.agent;

import org.example.agent.agent_callbacks.AgentCallback;

/**
 * agent应支持注册回调
 */
public interface IAgentCallBackUse {
    /**
     * 注册回调
     *
     * @param agentCallback agent回调
     */
    void registryAgentCallback(AgentCallback agentCallback);
}
