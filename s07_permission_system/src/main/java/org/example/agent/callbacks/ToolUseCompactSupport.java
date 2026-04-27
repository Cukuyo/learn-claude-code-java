package org.example.agent.callbacks;

import com.alibaba.fastjson2.JSONObject;
import org.example.agent.base.IAgent;
import org.example.queue.FixedSizeQueue;

/**
 * toolUse压缩，设置最大保留的tool返回数，当超过指定值时最开始的tool返回将被压缩
 */
public class ToolUseCompactSupport implements AgentCallback {
    private final FixedSizeQueue<JSONObject> toolUseResult;

    /**
     * @param toolUseResultRemain 最大保留的tool返回数
     */
    public ToolUseCompactSupport(int toolUseResultRemain) {
        this.toolUseResult = new FixedSizeQueue<>(toolUseResultRemain);
    }

    @Override
    public void callAfterToolUse(IAgent agent, String id, String name, JSONObject arguments, JSONObject toolMessage) {
        JSONObject oldest = this.toolUseResult.addWithLimit(toolMessage);
        if (oldest != null && oldest.getString("content").length() > 128) {
            oldest.put("content", "[早期工具结果已压缩。如果需要完整详情，请重新运行该工具。]");
        }
    }

}
