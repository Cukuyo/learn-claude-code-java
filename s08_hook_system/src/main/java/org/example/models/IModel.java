package org.example.models;

import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;

/**
 * 一个模型最基础的使用
 */
public interface IModel {
    JSONObject chat() throws IOException, InterruptedException;

    JSONObject addSystemMessages(String content);

    JSONObject addUserMessage(String content);

    JSONObject addToolMessage(String content, String toolCallId);

    void addAssistantMessages(JSONObject content);
}
