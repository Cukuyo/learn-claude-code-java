package org.example.agent;

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

    /**
     * 工具注册
     *
     * @param toolObj 实例类型tool
     */
    public void registryTool(Object toolObj);

    /**
     * 工具注册
     *
     * @param toolObj 静态方法类型tool
     */
    public void registryTool(Class<?> toolObj);

    /**
     * skills注册
     *
     * @param toolObj 静态方法类型tool
     */
    public void registrySkills(String path) throws IOException;
}
