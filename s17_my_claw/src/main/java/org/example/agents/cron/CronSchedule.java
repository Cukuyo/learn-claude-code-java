package org.example.agents.cron;

import org.example.agent.AgentCallback;
import org.example.agent.impl.AbstractAgent;
import org.example.agent.tool.ToolMethod;
import org.example.agent.tool.ToolParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 定时、延时调度
 */
public class CronSchedule implements AgentCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(CronSchedule.class);
    private static final ScheduledExecutorService AGENT_SCHEDULE = Executors.newScheduledThreadPool(
            0, Thread.ofPlatform().name("AgentWorker-Schedule", 0).factory());

    private AbstractAgent agent = null;
    private final Map<String, ScheduledFuture<?>> scheduledFutureMap = new ConcurrentHashMap<>();

    @Override

    public void initSelf(AbstractAgent agent) {
        this.agent = agent;
        agent.registryTool(this);
    }

    @Override
    public void removeSelf(AbstractAgent agent) {
        agent.removeTool(this);
    }

    @ToolMethod(description = "有些指令要等待一定时间后才能执行或获取，你可以为此添加延迟任务/提醒到闹钟")
    public String scheduleWithDelay(
            @ToolParam(description = "延迟任务/提醒名称，要求为驼峰形式") String name,
            @ToolParam(description = "延迟时间，单位为秒") int delay,
            @ToolParam(description = "延迟任务/提醒内容") String content) {
        if (scheduledFutureMap.containsKey(name)) {
            return "已有相同名称的延迟任务/提醒，请检查后重试";
        }

        ScheduledFuture<?> future = AGENT_SCHEDULE.schedule(() -> {
            try {
                String chatRsp = agent.chat(Collections.singletonList("闹钟"), Collections.singletonList(content));
                LOGGER.info("{} scheduleWithDelay {}S, {}, result: {}", agent.getAgentName(), delay, content, chatRsp);
            } catch (IOException | InterruptedException e) {
                LOGGER.info("{} scheduleWithDelay {}S, {} error", agent.getAgentName(), delay, content, e);
            } finally {
                scheduledFutureMap.remove(name);
            }
        }, delay, TimeUnit.SECONDS);

        scheduledFutureMap.put(name, future);

        return "添加成功";
    }

    @ToolMethod(description = "取消延迟任务/提醒到闹钟")
    public String cancelSchedule(
            @ToolParam(description = "延迟任务/提醒名称，要求为驼峰形式") String name) {
        ScheduledFuture<?> future = scheduledFutureMap.get(name);
        if (future != null) {
            future.cancel(true);
        }

        return "取消成功";
    }
}
