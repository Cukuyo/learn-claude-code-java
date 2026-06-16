package org.example.agents_company;

import org.example.agent.IAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 公司，agents打卡上下班的地方
 */
public class AgentCompany {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentCompany.class);
    private static final ThreadFactory THREAD_FACTORY = Thread.ofPlatform().name("AgentCompany", 0).factory();

    /**
     * agents工位
     */
    private final ExecutorService agentDesks = Executors.newThreadPerTaskExecutor(THREAD_FACTORY);

    /**
     * agents钉钉
     */
    private final Map<String, AgentWorker> agentsDingDing = new ConcurrentHashMap<>();

    /**
     * 打卡上班
     */
    public void clockIn(IAgent agent) {
        AgentWorker agentWorker = new AgentWorker(agent);
        agentsDingDing.put(agent.getAgentName(), agentWorker);
        agentDesks.submit(agentWorker);
    }

    /**
     * 打卡下班
     */
    public void clockOut() {
        for (AgentWorker agentWorker : agentsDingDing.values()) {
            agentWorker.clockOut();
        }
        agentsDingDing.clear();
    }

    public String dingDingByAdmin(String agentName, String name, String content) {
        if (!agentsDingDing.containsKey(agentName)) {
            return "<" + agentName + ">不存在，请检查后重试";
        }
        return agentsDingDing.get(agentName).dingDing(new ChatMessage(ChatMessageType.ADMIN, name, content));
    }

    public String dingDingByAgent(String agentName, String name, String content) {
        if (!agentsDingDing.containsKey(agentName)) {
            return "<" + agentName + ">不存在，请检查后重试";
        }
        return agentsDingDing.get(agentName).dingDing(new ChatMessage(ChatMessageType.AGENT, name, content));
    }

    public String dingDingByAsyncTool(String agentName, String name, String content) {
        if (!agentsDingDing.containsKey(agentName)) {
            return "<" + agentName + ">不存在，请检查后重试";
        }
        return agentsDingDing.get(agentName).dingDing(new ChatMessage(ChatMessageType.ASYNC_TOOL, name, content));
    }

    public String dingDingByCron(String agentName, String name, String content) {
        if (!agentsDingDing.containsKey(agentName)) {
            return "<" + agentName + ">不存在，请检查后重试";
        }
        return agentsDingDing.get(agentName).dingDing(new ChatMessage(ChatMessageType.CRON, name, content));
    }

    public String dingDingByHeart(String agentName, String name, String content) {
        if (!agentsDingDing.containsKey(agentName)) {
            return "<" + agentName + ">不存在，请检查后重试";
        }
        return agentsDingDing.get(agentName).dingDing(new ChatMessage(ChatMessageType.HEART, name, content));
    }

    /**
     * 一直工作的员工
     */
    static class AgentWorker implements Runnable {
        private final IAgent agent;
        private final PriorityBlockingQueue<ChatMessage> msgQueue = new PriorityBlockingQueue<>(16);
        private volatile boolean isStop = false;

        public AgentWorker(IAgent agent) {
            this.agent = agent;
        }

        public String dingDing(ChatMessage chatMessage) {
            if (chatMessage.content.startsWith("/")) {
                return agent.command(chatMessage.name, chatMessage.content);
            }
            msgQueue.offer(chatMessage);
            return "消息发送成功";
        }

        /**
         * 打卡下班
         */
        public void clockOut() {
            isStop = true;
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