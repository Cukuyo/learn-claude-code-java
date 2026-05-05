package org.example.agent.agent_hooks;

import com.alibaba.fastjson2.JSONObject;
import org.example.agent.IAgent;

/**
 * hook agent的输入输出，返回null代表不hook
 */
public interface AgentHook {
    default JSONObject hookAddUserMessage(IAgent agent, String content) {
        return null;
    }

    default JSONObject hookChat(IAgent agent) {
        return null;
    }

    default String hookToolUse(IAgent agent, String id, String name, JSONObject arguments) {
        return null;
    }
}
