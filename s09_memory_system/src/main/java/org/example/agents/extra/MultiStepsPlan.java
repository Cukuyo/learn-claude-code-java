package org.example.agents.extra;

import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.define_agent.AgentCallback;
import org.example.define_agent.core.AbstractAgent;
import org.example.define_tool.ToolMethod;
import org.example.define_tool.ToolParam;

/**
 * 多步骤规划
 */
public class MultiStepsPlan implements AgentCallback {
    public enum ItemStatus {
        pending, in_progress, completed
    }

    public record PlanItem(@ToolParam(description = "任务项内容") String content,
                           @ToolParam(description = "任务项状态") ItemStatus status) {
    }

    private static final Map<ItemStatus, String> MARKER = new HashMap<>();

    static {
        MARKER.put(ItemStatus.pending, "[ ]");
        MARKER.put(ItemStatus.in_progress, "[>]");
        MARKER.put(ItemStatus.completed, "[x]");
    }

    private final List<PlanItem> cache = new ArrayList<>();
    private int rounds_since_update = 0;

    private boolean inited = false;
    private boolean useTodo = false;

    @Override
    public void eachAtomicInitFirst(AbstractAgent agent) {
        if (!inited) {
            agent.registryTool(this);
            inited = true;
        }
    }

    @Override
    public void callBeforeToolsUse(AbstractAgent agent) {
        useTodo = false;
    }

    @Override
    public void callBeforeToolUse(AbstractAgent agent, String id, String name, JSONObject arguments) {
        if (name.equals("updateTasks")) {
            useTodo = true;
        }
    }

    @Override
    public void callAfterToolsUse(AbstractAgent agent) {
        if (useTodo) {
            noteRoundReset();
        } else {
            noteRoundWithoutUpdate();
            String reminder = reminder();
            if (!reminder.isEmpty()) {
                agent.getModel().addUserMessage(reminder);
            }
        }
    }

    /**
     * 更新任务项
     *
     * @param planItems 任务项数组
     * @return 友好的视图
     */
    @ToolMethod(description = "保存多步骤任务的任务项，若当前执行的任务包含多个步骤，为防止执行出现偏差，需保存待执行的任务项，同时注意每个任务项执行完后刷新状态")
    public String updateTasks(@ToolParam(description = "任务项数组") PlanItem[] planItems) {
        cache.clear();
        Collections.addAll(cache, planItems);
        return render();
    }

    /**
     * 组装友好的视图
     *
     * @return 视图
     */
    private String render() {
        if (cache.isEmpty()) {
            return "No session plan yet.";
        }
        StringBuilder builder = new StringBuilder(cache.size() * 64);
        for (PlanItem planItem : cache) {
            builder.append(MARKER.get(planItem.status)).append(planItem.content).append(System.lineSeparator());
        }
        return builder.toString();
    }

    /**
     * 提醒
     *
     * @return 提醒
     */
    private String reminder() {
        if (cache.isEmpty()) {
            return "";
        }
        if (rounds_since_update <= 3) {
            return "";
        }
        return "<注意>执行下一步动作前刷新任务项</注意>";
    }

    /**
     * 增加未刷新的次数
     */
    private void noteRoundWithoutUpdate() {
        rounds_since_update++;
    }

    /**
     * 重置未刷新的次数
     */
    private void noteRoundReset() {
        rounds_since_update = 0;
    }
}
