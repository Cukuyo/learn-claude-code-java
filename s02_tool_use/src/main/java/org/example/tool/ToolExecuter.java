package org.example.tool;

import com.alibaba.fastjson2.JSONObject;

/**
 * tool执行体
 */
@FunctionalInterface
public interface ToolExecuter {
    /**
     * 执行tool
     *
     * @param args llm返回的请求体
     * @return 执行结果
     */
    String execute(JSONObject args);
}

