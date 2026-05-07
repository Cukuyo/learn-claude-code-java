package org.example.agent.agent_commands;

import org.example.agent.agent_base.AbstractAgent;

import java.io.IOException;

/**
 * agent命令解析使用
 */
public interface AgentCommand {
    /**
     * 是否支持当前命令
     *
     * @param agent agent
     * @param cmd   cmd
     * @return 支持结果
     */
    boolean isSupportCommand(AbstractAgent agent, String cmd);

    /**
     * 执行当前命令
     *
     * @param agent agent
     * @param cmd   cmd
     * @return 执行结果
     */
    String command(AbstractAgent agent, String cmd) throws IOException;

    AgentCommand EMPTY = new AgentCommand() {
        @Override
        public boolean isSupportCommand(AbstractAgent agent, String cmd) {
            return false;
        }

        @Override
        public String command(AbstractAgent agent, String cmd) throws IOException {
            return null;
        }
    };
}
