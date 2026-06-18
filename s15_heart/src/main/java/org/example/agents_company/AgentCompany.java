package org.example.agents_company;

import org.example.agent.IAgent;
import org.example.agent.tool.ToolMethod;
import org.example.agent.tool.ToolParam;
import org.example.agents.MyAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 公司，agents打卡上下班的地方
 */
public class AgentCompany implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentCompany.class);

    /**
     * 最长摸鱼时间
     */
    private int maxIdleSeconds = 30 * 60;

    /**
     * 黑心资本家
     */
    private Future<String> superviseAgent;

    /**
     * agents工位
     */
    private final ExecutorService agentDesks;

    /**
     * agents钉钉
     */
    private final Map<String, AgentWorker> dingDing = new ConcurrentHashMap<>();

    public AgentCompany(String name) {
        this.agentDesks = Executors.newThreadPerTaskExecutor(Thread.ofPlatform().name(name, 0).factory());
        this.superviseAgent = this.agentDesks.submit(() -> {
            while (true) {
                for (String agentName : dingDing.keySet()) {
                    String dingDingRsp = dingDingByHeart(agentName, "HEART", """
                            [心跳调度]
                            收到此消息时检查是否有未完成任务、任务进度是否未刷新，若有则按优先级依次执行，若没有则保持休眠。
                            """);
                    LOGGER.info("{} -> {} : {}", "HEART", agentName, dingDingRsp);
                }
                try {
                    TimeUnit.SECONDS.sleep(getMaxIdleSeconds());
                } catch (InterruptedException e) {
                    break;
                }
            }
            return "";
        });
    }

    public int getMaxIdleSeconds() {
        return maxIdleSeconds;
    }

    public void setMaxIdleSeconds(int maxIdleSeconds) {
        this.maxIdleSeconds = maxIdleSeconds;
    }

    /**
     * 打卡上班
     */
    public void clockIn(IAgent agent) {
        AgentWorker agentWorker = new AgentWorker(agent);
        agentDesks.submit(agentWorker);
        dingDing.put(agent.getAgentName(), agentWorker);
    }

    /**
     * 打卡下班
     */
    public void clockOut(IAgent agent) {
        clockOut(agent.getAgentName());
    }

    /**
     * 打卡下班
     */
    public void clockOut(String agentName) {
        AgentWorker agentWorker = dingDing.get(agentName);
        dingDing.remove(agentName);
        if (agentWorker != null) {
            agentWorker.clockOut();
        }
    }

    @Override
    public void close() {
        agentDesks.shutdown();
        superviseAgent.cancel(true);
        for (String agentName : dingDing.keySet()) {
            clockOut(agentName);
        }
        gracefulClose(agentDesks);
    }

    private void gracefulClose(ExecutorService executorService) {
        try {
            boolean allCompleted = executorService.awaitTermination(10, TimeUnit.SECONDS);
            if (!allCompleted) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String dingDingByAdmin(String targetAgentName, String senderName, String content) {
        if (!dingDing.containsKey(targetAgentName)) {
            return "<" + targetAgentName + ">不存在，请检查后重试";
        }
        return dingDing.get(targetAgentName).dingDing(new ChatMessage(ChatMessageType.ADMIN, senderName, content));
    }

    public String dingDingByAgent(String targetAgentName, String senderName, String content) {
        if (!dingDing.containsKey(targetAgentName)) {
            return "<" + targetAgentName + ">不存在，请检查后重试";
        }
        return dingDing.get(targetAgentName).dingDing(new ChatMessage(ChatMessageType.AGENT, senderName, content));
    }

    public String dingDingByHeart(String targetAgentName, String senderName, String content) {
        if (!dingDing.containsKey(targetAgentName)) {
            return "<" + targetAgentName + ">不存在，请检查后重试";
        }
        return dingDing.get(targetAgentName).dingDing(new ChatMessage(ChatMessageType.HEART, senderName, content));
    }

    /**
     * 一直工作的员工
     */
    static class AgentWorker implements Runnable {
        private static final ScheduledExecutorService AGENT_SCHEDULE = Executors.newScheduledThreadPool(
                0, Thread.ofPlatform().name("AgentWorker-Schedule", 0).factory());

        private final IAgent agent;
        private final PriorityBlockingQueue<ChatMessage> msgQueue = new PriorityBlockingQueue<>(16);
        private volatile boolean isStop = false;

        /**
         * 打卡上班
         */
        public AgentWorker(IAgent agent) {
            this.agent = agent;
            ((MyAgent) this.agent).getAgent().registryTool(this);
        }

        public String dingDing(ChatMessage chatMessage) {
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
            ((MyAgent) this.agent).getAgent().removeTool(this);
        }

        @ToolMethod(description = "添加延迟任务/提醒到闹钟")
        public String cron(@ToolParam(description = "延迟时间，单位为秒") int delay,
                           @ToolParam(description = "延迟任务/提醒内容") String content) {
            AGENT_SCHEDULE.schedule(() -> {
                dingDing(new ChatMessage(ChatMessageType.CRON, agent.getAgentName(), content));
            }, delay, TimeUnit.SECONDS);
            LOGGER.info("{} add cron, {} s: {}", agent.getAgentName(), delay, content);

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
                        Thread.sleep(Duration.ofSeconds(1L));
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
}