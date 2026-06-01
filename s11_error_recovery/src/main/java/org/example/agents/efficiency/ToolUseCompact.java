package org.example.agents.efficiency;

import com.alibaba.fastjson2.JSONObject;

import org.example.framework_agent.AgentCallback;
import org.example.framework_agent.core.AbstractAgent;
import org.example.queue.FixedSizeQueue;

/**
 * toolUse压缩，设置最大保留的tool返回数，当超过指定值时最开始的tool返回将被压缩
 */
public class ToolUseCompact implements AgentCallback {
    private final FixedSizeQueue<JSONObject> middleCompactQueue;
    private final FixedSizeQueue<JSONObject> finalCompactQueue;

    /**
     * @param middleCompact 中段压缩大小。
     *                      首先进入中段压缩，达到上限后弹出最先进入的，压缩为abc->a[早期工具结果已截断。如果需要完整详情，请重新运行该工具。]c
     * @param finalCompact  最终压缩大小。
     *                      中段压缩后的进入最终压缩，达到上限后弹出最先进入的，
     *                      压缩为a[早期工具结果已截断。如果需要完整详情，请重新运行该工具。]c->[早期工具结果已压缩。如果需要完整详情，请重新运行该工具。]
     */
    public ToolUseCompact(int middleCompact, int finalCompact) {
        this.middleCompactQueue = new FixedSizeQueue<>(middleCompact);
        this.finalCompactQueue = new FixedSizeQueue<>(finalCompact);
    }

    @Override
    public void callAfterToolUse(AbstractAgent agent, String id, String name, JSONObject arguments, JSONObject toolMessage) {
        if (toolMessage.getString("content").length() <= 128) {
            return;
        }

        JSONObject middleCompact = middleCompactQueue.addWithLimit(toolMessage);
        if (middleCompact == null) {
            return;
        }
        String content = middleCompact.getString("content");
        int size = content.length() / 3;
        String middleCompactContent = content.substring(0, size)
                + "......[早期工具结果已截断。如果需要完整详情，请重新运行该工具。]......"
                + content.substring(size * 2);
        middleCompact.put("content", middleCompactContent);

        JSONObject finalCompact = finalCompactQueue.addWithLimit(middleCompact);
        if (finalCompact == null) {
            return;
        }
        middleCompact.put("content", "[早期工具结果已压缩。如果需要完整详情，请重新运行该工具。]");
    }

}
