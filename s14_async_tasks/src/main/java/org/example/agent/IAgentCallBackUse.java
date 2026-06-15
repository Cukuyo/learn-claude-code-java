package org.example.agent;

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
