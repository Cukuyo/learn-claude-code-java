package org.example.agent.callbacks;

import com.alibaba.fastjson2.JSONObject;
import org.example.agent.base.IAgent;

/**
 * agent-loop生命周期回调
 */
public interface AgentCallback {
    /**
     * 每次接到用户输入时执行的原子初始化，执行序列第一
     *
     * @param agent agent
     */
    default void eachAtomicInitFirst(IAgent agent) {
    }

    /**
     * 每次接到用户输入时执行的校验工作，执行序列第二
     *
     * @param agent agent
     */
    default void eachCheckSecond(IAgent agent) {
    }

    /**
     * 模型chat回调
     *
     * @param agent agent
     */
    default void callBeforeChat(IAgent agent) {
    }

    /**
     * 模型chat回调
     *
     * @param agent   agent
     * @param chatRsp chat rsp
     */
    default void callAfterChat(IAgent agent, JSONObject chatRsp, JSONObject message) {
    }

    /**
     * 工具使用回调
     *
     * @param agent agent
     */
    default void callBeforeToolsUse(IAgent agent) {
    }

    /**
     * 工具使用回调
     *
     * @param agent agent
     */
    default void callAfterToolsUse(IAgent agent) {
    }

    /**
     * 单个工具使用回调
     *
     * @param agent     agent
     * @param id        tool id
     * @param name      tool name
     * @param arguments tool args
     */
    default void callBeforeToolUse(IAgent agent, String id, String name, JSONObject arguments) {
    }

    /**
     * 单个工具使用回调
     *
     * @param agent     agent
     * @param id        tool id
     * @param name      tool name
     * @param arguments tool args
     * @param toolRsp   toolRsp
     */
    default void callAfterToolUse(IAgent agent, String id, String name, JSONObject arguments, String toolRsp) {
    }
}
