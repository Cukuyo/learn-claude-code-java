package org.example.agent;

import org.example.models.AbstractModel;

import java.io.IOException;

/**
 * agent最基础的支持
 */
public interface IAgent {
    AbstractModel getModel();

    String getAgentName();

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
