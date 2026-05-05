package org.example.agent;

import org.example.agent.agent_hooks.AgentHook;

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
