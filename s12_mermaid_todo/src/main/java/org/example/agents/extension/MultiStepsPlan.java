package org.example.agents.extension;

import com.alibaba.fastjson2.JSONObject;
import org.example.framework_agent.AgentCallback;
import org.example.framework_agent.core.AbstractAgent;
import org.example.framework_tool.ToolMethod;
import org.example.framework_tool.ToolParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 多步骤规划
 */
public class MultiStepsPlan implements AgentCallback {
    private final List<String> cache = new ArrayList<>();
    private int rounds_since_update = 0;

    private boolean useTodo = false;

    @Override
    public void eachAtomicInitFirst(AbstractAgent agent) {
        agent.registryTool(this);
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
    @ToolMethod(description = "更新任务项记事本。若当前执行的任务包含多个连贯的步骤，你可以使用此tool进行保存，防止长时间执行时出现偏差")
    public String updateTasks(@ToolParam(description = "任务项数组") String[] planItems) {
        cache.clear();
        Collections.addAll(cache, planItems);
        return "保存成功！";
    }

    /**
     * 更新任务项
     *
     * @return 友好的视图
     */
    @ToolMethod(description = "查看任务项记事本。若当前执行的任务包含多个连贯的步骤，你可以使用此tool进行查看，防止长时间执行时出现偏")
    public String lookUpTasks() {
        return render();
    }

    /**
     * 组装友好的视图
     *
     * @return 视图
     */
    private String render() {
        if (cache.isEmpty()) {
            return "当前无任务项";
        }
        StringBuilder builder = new StringBuilder(cache.size() * 64);
        for (String planItem : cache) {
            builder.append(planItem).append(System.lineSeparator());
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
