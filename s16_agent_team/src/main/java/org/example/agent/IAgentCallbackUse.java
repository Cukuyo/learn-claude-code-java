package org.example.agent;

/**
 * agent应支持注册回调
 */
public interface IAgentCallbackUse {
    /**
     * 注册回调
     *
     * @param agentCallback agent回调
     */
    void registryAgentCallback(AgentCallback agentCallback);

    /**
     * 移除回调
     *
     * @param agentCallback agent回调
     */
    void removeAgentCallback(AgentCallback agentCallback);
}
