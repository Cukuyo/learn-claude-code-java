package org.example.agent.agent_hooks;

import com.alibaba.fastjson2.JSONObject;
import org.example.agent.agent_base.AbstractAgent;

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
