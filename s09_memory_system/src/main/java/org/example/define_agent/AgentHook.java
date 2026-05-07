package org.example.define_agent;

import org.example.define_agent.core.AbstractAgent;

import com.alibaba.fastjson2.JSONObject;

/**
 * hook agent的输入输出，返回null代表不hook
 */
public interface AgentHook {
    default String hookCommand(AbstractAgent agent, String content) {
        return null;
    }

    default JSONObject hookAddUserMessage(AbstractAgent agent, String content) {
        return null;
    }

    default JSONObject hookChat(AbstractAgent agent) {
        return null;
    }

    default String hookToolUse(AbstractAgent agent, String id, String name, JSONObject arguments) {
        return null;
    }
}
