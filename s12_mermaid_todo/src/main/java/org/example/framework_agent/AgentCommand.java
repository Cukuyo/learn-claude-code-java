package org.example.framework_agent;

import java.io.IOException;

import org.example.framework_agent.core.AbstractAgent;

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
