package org.example.agents;

import org.example.agent.AgentCommand;
import org.example.agent.impl.AbstractAgent;

import java.util.StringJoiner;

/**
 * /list
 */
public class ListCommand implements AgentCommand {
    @Override
    public String desc(AbstractAgent agent) {
        return "/list 列出当前支持命令";
    }

    @Override
    public String help(AbstractAgent agent) {
        return """
                /list 列出当前支持命令
                """;
    }

    @Override
    public boolean isSupportCommand(AbstractAgent agent, String cmd) {
        return cmd.trim().split("\\s+")[0].equals("/list");
    }

    @Override
    public String command(AbstractAgent agent, String cmd) {
        String[] arr = cmd.trim().split("\\s+");
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].trim();
        }

        if (arr.length == 1) {
            return show(agent);
        }

        return "不支持的命令参数！";
    }

    private String show(AbstractAgent agent) {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("");
        for (AgentCommand agentCommand : agent.agentCommands) {
            joiner.add(agentCommand.desc(agent));
        }
        return joiner.toString();
    }
}
