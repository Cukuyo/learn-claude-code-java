package org.example.agent.agent_callbacks;

import com.alibaba.fastjson2.JSONObject;
import org.example.agent.agent_base.AbstractAgent;

/**
 * agent-loop生命周期回调
 */
public interface AgentCallback {
    /**
     * 每次接到用户输入时执行的原子初始化，执行序列第一
     *
     * @param agent agent
     */
    default void eachAtomicInitFirst(AbstractAgent agent) {
    }

    /**
     * 每次接到用户输入时执行的校验工作，执行序列第二
     *
     * @param agent agent
     */
    default void eachCheckSecond(AbstractAgent agent) {
    }

    /**
     * 添加执行命令前的回调
     *
     * @param agent   agent
     * @param content command body
     */
    default void callBeforeCommand(AbstractAgent agent, String content) {
    }

    /**
     * 添加执行命令后的回调
     *
     * @param agent      agent
     * @param content    command body
     * @param commandRsp command rsp
     */
    default void callAfterCommand(AbstractAgent agent, String content, String commandRsp) {
    }

    /**
     * 添加用户提示后的回调
     *
     * @param agent       agent
     * @param userMessage userMessage
     */
    default void callAfterAddUserMessage(AbstractAgent agent, JSONObject userMessage) {
    }

    /**
     * 模型chat回调
     *
     * @param agent agent
     */
    default void callBeforeChat(AbstractAgent agent) {
    }

    /**
     * 模型chat回调
     *
     * @param agent   agent
     * @param chatRsp chat rsp
     */
    default void callAfterChat(AbstractAgent agent, JSONObject chatRsp, JSONObject assistantMessage) {
    }

    /**
     * 工具使用回调
     *
     * @param agent agent
     */
    default void callBeforeToolsUse(AbstractAgent agent) {
    }

    /**
     * 工具使用回调
     *
     * @param agent agent
     */
    default void callAfterToolsUse(AbstractAgent agent) {
    }

    /**
     * 单个工具使用回调
     *
     * @param agent     agent
     * @param id        tool id
     * @param name      tool name
     * @param arguments tool args
     */
    default void callBeforeToolUse(AbstractAgent agent, String id, String name, JSONObject arguments) {
    }

    /**
     * 单个工具使用回调
     *
     * @param agent       agent
     * @param id          tool id
     * @param name        tool name
     * @param arguments   tool args
     * @param toolMessage toolMessage
     */
    default void callAfterToolUse(AbstractAgent agent, String id, String name, JSONObject arguments, JSONObject toolMessage) {
    }
}
