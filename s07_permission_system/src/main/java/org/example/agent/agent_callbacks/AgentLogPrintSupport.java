package org.example.agent.agent_callbacks;

import org.example.agent.IAgent;

import com.alibaba.fastjson2.JSONObject;

/**
 * 日志打印回调
 */
public class AgentLogPrintSupport implements AgentCallback {
    public static final AgentLogPrintSupport INSTANCE = new AgentLogPrintSupport();

    @Override
    public void callBeforeToolUse(IAgent agent, String id, String name, JSONObject arguments) {
        System.out.printf("<%s> 开始执行tool, id:%s, func:%s, args:%s %s", agent.getAgentName(), id, name, arguments, System.lineSeparator());
    }

    @Override
    public void callAfterToolUse(IAgent agent, String id, String name, JSONObject arguments, JSONObject toolMessage) {
        System.out.printf("<%s> 结束执行tool, id:%s, func:%s, args:%s , result:%s %s", agent.getAgentName(), id, name, arguments, toolMessage, System.lineSeparator());
    }

    @Override
    public void callAfterChat(IAgent agent, JSONObject chatRsp, JSONObject assistantMessage) {
        System.out.printf("%s>>>%s%s", agent.getAgentName(), assistantMessage.getString("content"), System.lineSeparator());
    }
}
