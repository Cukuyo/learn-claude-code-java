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
     * 定时器次之
     */
    CRON(3),
    /*
     * 心跳最后
     */
    HEART(4),
    ;
    public final int priority;

    ChatMessageType(int priority) {
        this.priority = priority;
    }
}
