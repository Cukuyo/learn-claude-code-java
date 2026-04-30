package org.example.agent;

import org.example.agent.callbacks.AgentCallback;
import org.example.models.AbstractModel;

import java.io.IOException;

/**
 * agent统一接口
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

    /**
     * 工具注册
     *
     * @param toolObj 实例类型tool
     */
    void registryTool(Object toolObj);

    /**
     * 工具注册
     *
     * @param toolObj 静态方法类型tool
     */
    void registryTool(Class<?> toolObj);

    /**
     * skill注册
     *
     * @param dirPath skill.md所在目录
     */
    void registrySkills(String dirPath);

    /**
     * 注册回调
     *
     * @param agentCallback agent回调
     */
    void registryAgentCallback(AgentCallback agentCallback);
}
