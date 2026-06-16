package org.example.agents.log;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.agent.AgentCallback;
import org.example.agent.impl.AbstractAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 日志打印回调
 */
public class AgentLogPrint implements AgentCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentLogPrint.class);

    public static final AgentLogPrint INSTANCE = new AgentLogPrint();

    @Override
    public void callBeforeAgentLoop(AbstractAgent agent, JSONArray messages, List<JSONObject> userMessageList) {
        for (JSONObject userMessage : userMessageList) {
            LOGGER.info("{} -> {} : {}", userMessage.getString("name", ""), agent.getAgentName(), userMessage.getString("content"));
        }
    }

    @Override
    public void callBeforeToolUse(AbstractAgent agent, String id, String name, JSONObject arguments) {
        LOGGER.info("{} 开始执行tool, id: {}, func: {}, args: {}", agent.getAgentName(), id, name, arguments);
    }

    @Override
    public void callAfterToolUse(AbstractAgent agent, String id, String name, JSONObject arguments, JSONObject toolMessage) {
        LOGGER.info("{} 结束执行tool, id: {}, func: {}, args: {} , result: {}", agent.getAgentName(), id, name, arguments, toolMessage);
    }

    @Override
    public void callAfterChat(AbstractAgent agent, JSONObject chatRsp, JSONObject assistantMessage, boolean finished) {
        if (assistantMessage.containsKey("reasoning_content")) {
            LOGGER.info("{} >>> ({})", agent.getAgentName(), assistantMessage.getString("reasoning_content"));
        }
        LOGGER.info("{} >>> {}", agent.getAgentName(), assistantMessage.getString("content"));
    }
}
