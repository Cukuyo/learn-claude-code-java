package org.example.todo;

import org.example.tool.ToolMethod;
import org.example.tool.ToolParam;

import java.util.*;

/**
 * 让AI记录下多步骤的事项
 */
public class TodoManager {
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

    /**
     * 更新任务项
     *
     * @param planItems 任务项数组
     * @return 友好的视图
     */
    @ToolMethod(description = "Rewrite the current session plan for multi-step work.")
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
    public String render() {
        if (cache.isEmpty()) {
            return "No session plan yet.";
        }
        StringBuilder builder = new StringBuilder(cache.size() * 64);
        for (PlanItem planItem : cache) {
            builder.append(MARKER.get(planItem.status));
            builder.append(planItem.content);
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

    /**
     * 提醒
     *
     * @return 提醒
     */
    public String reminder() {
        if (cache.isEmpty()) {
            return "";
        }
        if (rounds_since_update <= 3) {
            return "";
        }
        return "<reminder>Refresh your current plan before continuing.</reminder>";
    }

    /**
     * 增加未刷新的次数
     */
    public void note_round_without_update() {
        rounds_since_update++;
    }
}
