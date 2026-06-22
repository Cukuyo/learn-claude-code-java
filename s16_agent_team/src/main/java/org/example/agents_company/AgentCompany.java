package org.example.agents_company;

import org.example.agent.IAgent;
import org.example.agent.impl.AbstractAgent;
import org.example.agent.tool.ToolMethod;
import org.example.agent.tool.ToolParam;
import org.example.agents.MyAgent;
import org.example.agents_company.worker.AgentWorker;
import org.example.agents_company.worker.IAgentWorker;
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
     * 公司名称
     */
    private final String companyName;

    /**
     * 公司目标
     */
    private final String companyRole;

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
    private final Map<String, IAgentWorker> dingDing = new ConcurrentHashMap<>();

    public AgentCompany(String companyName, String companyRole) {
        this.companyName = companyName;
        this.companyRole = companyRole;
        this.agentDesks = Executors.newThreadPerTaskExecutor(Thread.ofPlatform().name(companyName, 0).factory());
        this.superviseAgent = this.agentDesks.submit(() -> {
            while (true) {
                for (String agentName : dingDing.keySet()) {
                    String dingDingRsp = dingDing.get(agentName).dingDing(new ChatMessage(ChatMessageType.HEART, "HEART", """
                            [心跳调度]
                            收到此消息时检查是否有未完成任务、任务进度是否未刷新，若有则按优先级依次执行，若没有则保持休眠。
                            """));
                    LOGGER.info("{} -> {} : {} : {}", "HEART", agentName, "[心跳调度]", dingDingRsp);
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
    public void clockIn(IAgent agent, String agentJobPosition, String agentDuties) {
        if (agent instanceof AbstractAgent abstractAgent) {
            renderPrompt(abstractAgent);
        }
        if (agent instanceof MyAgent myAgent) {
            renderPrompt(myAgent.getAgent());
        }

        IAgentWorker agentWorker = new AgentWorker(agent, agentJobPosition, agentDuties);
        agentDesks.submit(agentWorker);
        dingDing.put(agent.getAgentName(), agentWorker);
    }

    private void renderPrompt(AbstractAgent abstractAgent) {
        abstractAgent.registryTool(this);
        abstractAgent.getModel().addSystemMessages(String.format("[AgentCompany]你已加入<%s>，这是一家<%s>的公司，" +
                        "可以使用<listWorkmates>获取同事列表，可以使用<dingDingByAgent>进行任务分派、协作、汇报、信息同步等",
                companyName, companyRole));
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
        IAgentWorker agentWorker = dingDing.get(agentName);
        dingDing.remove(agentName);
        if (agentWorker == null) {
            return;
        }
        if (agentWorker.getAgent() instanceof AbstractAgent abstractAgent) {
            abstractAgent.removeTool(this);
            abstractAgent.getModel().addSystemMessages(String.format("[AgentCompany]你已退出<%s>", companyName));
        }
        if (agentWorker.getAgent() instanceof MyAgent myAgent) {
            myAgent.getAgent().removeTool(this);
            myAgent.getAgent().getModel().addSystemMessages(String.format("[AgentCompany]你已退出<%s>", companyName));
        }
        agentWorker.clockOut();
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

    @ToolMethod(description = "获取同事列表")
    public String listWorkmates() {
        StringBuilder builder = new StringBuilder(dingDing.size() * 128);
        dingDing.values().forEach(cv -> builder.append(cv.toPrompt()).append(System.lineSeparator()));
        return builder.toString();
    }

    @ToolMethod(description = "向同事发送消息，进行任务分派、协作、汇报、信息同步等")
    public String dingDingByAgent(
            @ToolParam(description = "目标同事名字") String targetAgentName,
            @ToolParam(description = "你的名字") String senderName,
            @ToolParam(description = "消息内容") String content) {
        if (!dingDing.containsKey(targetAgentName)) {
            return "<" + targetAgentName + ">不存在，请检查后重试";
        }
        return dingDing.get(targetAgentName).dingDing(new ChatMessage(ChatMessageType.AGENT, senderName, content));
    }

    public String dingDingByAdmin(String targetAgentName, String senderName, String content) {
        if (!dingDing.containsKey(targetAgentName)) {
            return "<" + targetAgentName + ">不存在，请检查后重试";
        }
        return dingDing.get(targetAgentName).dingDing(new ChatMessage(ChatMessageType.ADMIN, senderName, content));
    }
}