package org.example.agents_company;

import org.example.agent.IAgent;
import org.example.agent.impl.AbstractAgent;
import org.example.agent.tool.ToolMethod;
import org.example.agent.tool.ToolParam;
import org.example.agents.MyAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 一直工作的员工
 */
class AgentWorker implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentWorker.class);
    private static final ScheduledExecutorService AGENT_SCHEDULE = Executors.newScheduledThreadPool(
            0, Thread.ofPlatform().name("AgentWorker-Schedule", 0).factory());

    private final IAgent agent;
    private final String agentJobPosition;
    private final String agentDuties;
    private final PriorityBlockingQueue<ChatMessage> msgQueue = new PriorityBlockingQueue<>(16);
    private volatile boolean isStop = false;

    /**
     * 打卡上班
     *
     * @param agent            打工人
     * @param agentJobPosition 岗位
     * @param agentDuties      职责
     */
    public AgentWorker(IAgent agent, String agentJobPosition, String agentDuties) {
        this.agent = agent;
        this.agentJobPosition = agentJobPosition;
        this.agentDuties = agentDuties;
        if (agent instanceof AbstractAgent abstractAgent) {
            renderPrompt(agentJobPosition, agentDuties, abstractAgent);
        }
        if (agent instanceof MyAgent myAgent) {
            renderPrompt(agentJobPosition, agentDuties, myAgent.getAgent());
        }
    }

    private void renderPrompt(String agentJobPosition, String agentDuties, AbstractAgent abstractAgent) {
        abstractAgent.registryTool(this);
        abstractAgent.getModel().addSystemMessages(String.format("你的岗位是：%s，职责是：%s", agentJobPosition, agentDuties));
    }

    /**
     * toPrompt
     *
     * @return Prompt
     */
    public String toPrompt() {
        return "- {" + agent.getAgentName() + ":" + agentJobPosition + ":" + agentDuties + "}";
    }

    /**
     * 工作
     */
    public String dingDing(ChatMessage chatMessage) {
        if (isStop) {
            return "当前agent已停止接收消息";
        }

        if (chatMessage.content.startsWith("/")) {
            return agent.command(chatMessage.name, chatMessage.content);
        }

        // 有事儿干就不用心跳机制关心了
        if (chatMessage.chatMessageType == ChatMessageType.HEART && !msgQueue.isEmpty()) {
            return "消息发送成功";
        }

        msgQueue.offer(chatMessage);
        return "消息发送成功";
    }

    /**
     * 打卡下班
     */
    public void clockOut() {
        isStop = true;
        if (agent instanceof AbstractAgent) {
            ((AbstractAgent) agent).removeTool(this);
        }
        if (agent instanceof MyAgent) {
            ((MyAgent) agent).getAgent().removeTool(this);
        }
    }

    public IAgent getAgent() {
        return agent;
    }

    public String getAgentJobPosition() {
        return agentJobPosition;
    }

    public String getAgentDuties() {
        return agentDuties;
    }

    @ToolMethod(description = "有些指令要等待一定时间后才能执行或获取，你可以为此添加延迟任务/提醒到闹钟")
    public String cron(@ToolParam(description = "延迟时间，单位为秒") int delay,
                       @ToolParam(description = "延迟任务/提醒内容") String content) {
        AGENT_SCHEDULE.schedule(() -> {
            String dingDingRsp = dingDing(new ChatMessage(ChatMessageType.CRON, agent.getAgentName(), content));
            LOGGER.info("{} cron dingDing, {} : {}", agent.getAgentName(), content, dingDingRsp);
        }, delay, TimeUnit.SECONDS);

        return "添加成功";
    }

    @Override
    public void run() {
        while (!isStop) {
            List<ChatMessage> list = new ArrayList<>();
            msgQueue.drainTo(list);
            // 没事儿就摸会儿鱼
            if (list.isEmpty()) {
                try {
                    Thread.sleep(Duration.ofSeconds(3L));
                } catch (InterruptedException e) {
                    break;
                }

                continue;
            }

            List<String> nameList = new ArrayList<>(list.size());
            List<String> chatContentList = new ArrayList<>(list.size());
            for (ChatMessage chatMessage : list) {
                nameList.add(chatMessage.name);
                chatContentList.add(chatMessage.content);
            }

            // 工作
            try {
                agent.chat(nameList, chatContentList);
            } catch (IOException | InterruptedException e) {
                LOGGER.error("{} chat 发生错误!", agent.getAgentName(), e);
            }
        }

        LOGGER.info("{} clockOut", agent.getAgentName());
    }
}
