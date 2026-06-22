package org.example.agents_company;

import org.example.agent.IAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
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
    private final Future<String> superviseAgent;

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

        try {
            boolean allCompleted = agentDesks.awaitTermination(30, TimeUnit.SECONDS);
            if (!allCompleted) {
                agentDesks.shutdownNow();
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
}