package org.example.agent.hooks;

import org.example.agent.IAgent;

import com.alibaba.fastjson2.JSONObject;

public interface AgentHook {
    default JSONObject hookAddUserMessage(IAgent agent, String content){
        return null;
    }

    default JSONObject hookChat(IAgent agent){
        return null;
    }

    default String hookToolUse(IAgent agent, String id, String name, JSONObject arguments){
        return null;
    }
}
