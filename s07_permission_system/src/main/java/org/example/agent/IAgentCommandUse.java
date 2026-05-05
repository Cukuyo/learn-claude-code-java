package org.example.agent;

import org.example.agent.agent_command.AgentCommand;

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
