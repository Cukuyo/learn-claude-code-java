package org.example.agent.tool;

import com.alibaba.fastjson2.JSONObject;

import java.util.concurrent.*;

/**
 * tool执行体
 */
@FunctionalInterface
public interface ToolExecuter {
    /**
     * 简单的同步返回
     *
     * @param rsp rsp
     * @return 同步返回
     */
    static Future<String> simpleRsp(String rsp) {
        return new Future<>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return true;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public String get() {
                return rsp;
            }

            @Override
            public String get(long timeout, TimeUnit unit) {
                return rsp;
            }
        };
    }

    /**
     * 执行tool
     *
     * @param args llm返回的请求体
     * @return 执行结果
     */
    Future<String> execute(JSONObject args);
}

