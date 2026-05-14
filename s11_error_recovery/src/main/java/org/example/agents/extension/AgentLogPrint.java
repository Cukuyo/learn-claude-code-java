package org.example.agents.extension;

import org.example.framework_agent.AgentCallback;
import org.example.framework_agent.core.AbstractAgent;

import com.alibaba.fastjson2.JSONObject;

/**
 * 日志打印回调
 */
public class AgentLogPrint implements AgentCallback {
    public static final AgentLogPrint INSTANCE = new AgentLogPrint();

    @Override
    public void callBeforeToolUse(AbstractAgent agent, String id, String name, JSONObject arguments) {
        System.out.printf("<%s> 开始执行tool, id:%s, func:%s, args:%s %s", agent.getAgentName(), id, name, arguments, System.lineSeparator());
    }

    @Override
    public void callAfterToolUse(AbstractAgent agent, String id, String name, JSONObject arguments, JSONObject toolMessage) {
        System.out.printf("<%s> 结束执行tool, id:%s, func:%s, args:%s , result:%s %s", agent.getAgentName(), id, name, arguments, toolMessage, System.lineSeparator());
    }

    @Override
    public void callAfterChat(AbstractAgent agent, JSONObject chatRsp, JSONObject assistantMessage, boolean finishied) {
        if (finishied) {
            return;
        }
        if (assistantMessage.containsKey("reasoning_content")) {
            System.out.printf("%s>>>(%s)%s", agent.getAgentName(), assistantMessage.getString("reasoning_content"), System.lineSeparator());
        }
        System.out.printf("%s>>>%s%s", agent.getAgentName(), assistantMessage.getString("content"), System.lineSeparator());
    }

    @Override
    public void callAfterCommand(AbstractAgent agent, String content, String commandRsp) {
        System.out.printf("%s>>>%s%s", agent.getAgentName(), commandRsp, System.lineSeparator());
    }

    @Override
    public void callAfterAgentLoop(AbstractAgent agent, JSONObject userMessage, String chatRsp) {
        System.out.printf("%s>>>%s%s", agent.getAgentName(), chatRsp, System.lineSeparator());
    }
}
