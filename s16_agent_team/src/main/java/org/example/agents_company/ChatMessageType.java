package org.example.agents_company;

/**
 * 聊天类型
 */
public enum ChatMessageType {
    /*
     * 管理员说话第一
     */
    ADMIN(1),
    /*
     * 其他agent次之
     */
    AGENT(2),
    /*
     * 异步工具次之
     */
    ASYNC_TOOL(3),
    /*
     * 定时器次之
     */
    CRON(4),
    /*
     * 心跳最后
     */
    HEART(5),
    ;
    public final int priority;

    ChatMessageType(int priority) {
        this.priority = priority;
    }
}
