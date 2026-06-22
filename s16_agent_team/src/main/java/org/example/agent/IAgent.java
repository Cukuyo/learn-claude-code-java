package org.example.agent;

import org.example.models.AbstractModel;

import java.io.IOException;
import java.util.List;

/**
 * agent最基础的支持
 */
public interface IAgent {
    AbstractModel getModel();

    String getAgentName();

    String getAgentRole();

    /**
     * 简单接口，聊天或者下命令
     *
     * @param content 内容
     * @return 响应
     * @throws IOException          io异常
     * @throws InterruptedException 等待被中断
     */
    String chatOrCommand(String content) throws IOException, InterruptedException;

    /**
     * 下命令
     *
     * @param command 会话内容
     * @return 响应
     */
    String command(String command);

    /**
     * 聊天
     *
     * @param chatContent 会话内容
     * @return 响应
     * @throws IOException          io异常
     * @throws InterruptedException 等待被中断
     */
    String chat(String chatContent) throws IOException, InterruptedException;

    /**
     * 下命令
     *
     * @param name    会话人名称
     * @param command 会话内容
     * @return 响应
     */
    String command(String name, String command);

    /**
     * 聊天，批量传入
     *
     * @param nameList        会话人名称
     * @param chatContentList 会话内容
     * @return 响应
     * @throws IOException          io异常
     * @throws InterruptedException 等待被中断
     */
    String chat(List<String> nameList, List<String> chatContentList) throws IOException, InterruptedException;
}
