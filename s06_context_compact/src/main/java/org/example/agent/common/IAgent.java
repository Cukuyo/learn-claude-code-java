package org.example.agent.common;

import java.io.IOException;

/**
 * agent统一接口
 */
public interface IAgent {
    /**
     * 聊天或者下命令
     *
     * @param content 内容
     * @return 响应
     * @throws IOException          io异常
     * @throws InterruptedException 等待被中断
     */
    String chatOrCommand(String content) throws IOException, InterruptedException;
}
