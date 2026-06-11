package org.example.framework_agent;

import org.example.framework_models.AbstractModel;

import java.io.IOException;

/**
 * agent最基础的支持
 */
public interface IAgent {
    AbstractModel getModel();

    String getAgentName();

    String getAgentRole();

    /**
     * 聊天或者下命令
     *
     * @param name    输入者
     * @param content 内容
     * @return 响应
     * @throws IOException          io异常
     * @throws InterruptedException 等待被中断
     */
    String chatOrCommand(String name, String content) throws IOException, InterruptedException;
}
