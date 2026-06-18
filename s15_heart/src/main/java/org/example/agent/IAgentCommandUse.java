package org.example.agent;

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

    /**
     * 命令移除
     *
     * @param agentCommand agentCommand
     */
    void removeCommand(AgentCommand agentCommand);
}
