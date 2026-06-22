package org.example.agents_company.worker;

import org.example.agent.IAgent;
import org.example.agents_company.ChatMessage;

/**
 * IAgentWorker
 */
public interface IAgentWorker extends Runnable {
    IAgent getAgent();

    String getAgentJobPosition();

    String getAgentDuties();

    /**
     * 描述提示词
     *
     * @return 描述提示词
     */
    String toPrompt();

    /**
     * 上班
     *
     * @param chatMessage chatMessage
     * @return 响应
     */
    String dingDing(ChatMessage chatMessage);

    /**
     * 打卡下班
     */
    void clockOut();
}
