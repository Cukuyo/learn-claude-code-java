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

    @ToolMethod(description = "Rewrite the current session plan for multi-step work.")
    public String update(@ToolParam(description = "任务项数组") String[] planItems) {
        cache.clear();
//        Collections.addAll(cache, planItems);
        return render();
    }

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

    public String reminder() {
        if (cache.isEmpty()) {
            return "";
        }
        if (rounds_since_update <= 3) {
            return "";
        }
        return "<reminder>Refresh your current plan before continuing.</reminder>";
    }

    public void note_round_without_update() {
        rounds_since_update++;
    }
}
