package org.example.framework_agent;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.framework_agent.core.AbstractAgent;

/**
 * hook agent的输入输出，返回null代表不hook
 */
public interface AgentHook {
    default String hookCommand(AbstractAgent agent, String name, String content) {
        return null;
    }

    default JSONObject hookAddUserMessage(AbstractAgent agent, String name, String content) {
        return null;
    }

    default JSONObject hookChat(AbstractAgent agent, JSONArray messages) {
        return null;
    }

    default String hookToolUse(AbstractAgent agent, String id, String name, JSONObject arguments) {
        return null;
    }
}
