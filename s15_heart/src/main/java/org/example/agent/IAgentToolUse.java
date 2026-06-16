package org.example.agent;

/**
 * agent应支持注册tool
 */
public interface IAgentToolUse {
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
}
