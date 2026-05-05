package org.example.agent.agent_command;

/**
 * agent命令解析使用
 */
public interface AgentCommand {
    /**
     * 是否支持当前命令
     *
     * @param cmd cmd
     * @return 支持结果
     */
    boolean isSupportCommand(String cmd);

    /**
     * 执行当前命令
     *
     * @param cmd cmd
     * @return 执行结果
     */
    String command(String cmd);
}
