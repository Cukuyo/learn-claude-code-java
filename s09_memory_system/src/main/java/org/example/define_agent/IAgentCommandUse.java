package org.example.define_agent;

/**
 * agent应支持注册命令
 */
public interface IAgentCommandUse {
    /**
     * 命令注册
     *
     * @param agentCommand agentCommand
     */
    void registryCommand(AgentCommand agentCommand);
}
