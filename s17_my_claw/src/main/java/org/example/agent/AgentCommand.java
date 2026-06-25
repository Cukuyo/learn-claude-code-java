package org.example.agent;

import org.example.agent.impl.AbstractAgent;

/**
 * agent命令解析使用
 */
public interface AgentCommand {
    /**
     * 当前命令描述
     *
     * @param agent agent
     * @return 命令描述
     */
    String desc(AbstractAgent agent);

    /**
     * 当前命令帮助
     *
     * @param agent agent
     * @return 命令帮助
     */
    String help(AbstractAgent agent);

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
    String command(AbstractAgent agent, String cmd);

    AgentCommand EMPTY = new AgentCommand() {
        @Override
        public String desc(AbstractAgent agent) {
            return "";
        }

        @Override
        public String help(AbstractAgent agent) {
            return "";
        }

        @Override
        public boolean isSupportCommand(AbstractAgent agent, String cmd) {
            return false;
        }

        @Override
        public String command(AbstractAgent agent, String cmd) {
            return null;
        }
    };
}
