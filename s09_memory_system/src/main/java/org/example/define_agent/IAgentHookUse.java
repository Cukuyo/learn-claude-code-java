package org.example.define_agent;

/**
 * agent应支持注册hook，相同hook仅第一个生效
 */
public interface IAgentHookUse {
    /**
     * hook注册
     *
     * @param agentHook agentHook
     */
    void registryHook(AgentHook agentHook);
}
