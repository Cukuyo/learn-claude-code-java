package org.example.framework_agent;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.framework_agent.core.AbstractAgent;

import java.util.List;

/**
 * agent-loop生命周期回调
 */
public interface AgentCallback {
    /**
     * 初始化操作
     *
     * @param agent agent
     */
    default void initSelf(AbstractAgent agent) {
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
     * agentLoop前回调，添加用户提示后的回调
     *
     * @param agent           agent
     * @param messages        messages
     * @param userMessageList userMessage
     */
    default void callBeforeAgentLoop(AbstractAgent agent, JSONArray messages, List<JSONObject> userMessageList) {
    }

    /**
     * agentLoop后回调，添加用户提示后直至响应完成
     *
     * @param agent           agent
     * @param messages        messages
     * @param userMessageList userMessage
     * @param chatRsp         chatRsp
     */
    default void callAfterAgentLoop(AbstractAgent agent, JSONArray messages, List<JSONObject> userMessageList, String chatRsp) {
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
    default void callAfterChat(AbstractAgent agent, JSONObject chatRsp, JSONObject assistantMessage, boolean finished) {
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
