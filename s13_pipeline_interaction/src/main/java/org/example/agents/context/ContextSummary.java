package org.example.agents.context;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.framework_agent.AgentCallback;
import org.example.framework_agent.IAgent;
import org.example.framework_agent.core.AbstractAgent;
import org.example.framework_models.AbstractModel;
import org.example.queues.FixedSizeConversationQueue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 上下文总结，设置最大上下文阈值x%，最近的会话保留数y。</p>
 * 若当前token数超过模型最大输入token*x%，除最近的y条会话外，其余用户会话将会总结压缩
 */
public class ContextSummary implements AgentCallback {
    private final double contextRemainRatio;

    private final FixedSizeConversationQueue recentConversations;
    private final List<JSONObject> olderThanRecentConversations = new LinkedList<>();
    private final Predicate<JSONObject> endPredicate = msg -> msg.getString("role").equalsIgnoreCase("user");

    public ContextSummary(double contextRemainRatio, int recentConversations) {
        this.contextRemainRatio = contextRemainRatio;
        this.recentConversations = new FixedSizeConversationQueue(recentConversations);
    }

    @Override
    public void callBeforeAgentLoop(AbstractAgent agent, JSONArray messages, JSONObject userMessage) {
        olderThanRecentConversations.addAll(recentConversations.add(userMessage, endPredicate));
    }

    @Override
    public void callAfterChat(AbstractAgent agent, JSONObject chatRsp, JSONObject assistantMessage, boolean finished) {
        olderThanRecentConversations.addAll(recentConversations.add(assistantMessage, endPredicate));
    }

    @Override
    public void callAfterToolUse(AbstractAgent agent, String id, String name, JSONObject arguments, JSONObject toolMessage) {
        olderThanRecentConversations.addAll(recentConversations.add(toolMessage, endPredicate));
    }

    @Override
    public void callBeforeChat(AbstractAgent agent) {
        long totalTokens = agent.getModel().getLastChatTotalTokens();
        double tokenThreshold = agent.getModel().getMaxInputTokens() * contextRemainRatio;
        if (totalTokens <= tokenThreshold) {
            return;
        }

        JSONArray messages = agent.getModel().getMessages();
        // 清空除系统提示词外的上下文
        messages.removeIf(message -> !((JSONObject) message).getString("role").equals("system"));

        try {
            // 获取压缩会话
            String forSummaryContext = buildSummaryContext(totalTokens, tokenThreshold);
            String summaryContext = summary(agent, forSummaryContext);
            // 先放入压缩的
            agent.getModel().addUserMessage(String.format("之前的对话已精简压缩，便于智能体继续开展工作。精简后内容如下：%s%s", System.lineSeparator(), summaryContext));
            // 再放入最近没压缩过的
            messages.addAll(recentConversations);

            System.out.printf("！！！上下文压缩已完成：压缩前预估tokens %d, 压缩后预估tokens %d %s",
                    forSummaryContext.length(), summaryContext.length(), System.lineSeparator());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildSummaryContext(long totalTokens, double tokenThreshold) {
        // 只是按比例简单估算下StringBuilder的容量，防止重复扩容
        double conversationNewOldRatio = 1.0d * olderThanRecentConversations.size() / (olderThanRecentConversations.size() + recentConversations.size());

        System.out.printf("！！！触发上下文压缩：model当前tokens数 %d, 阈值 %d, 压缩会话占比 %f %s",
                totalTokens, (int) tokenThreshold, conversationNewOldRatio, System.lineSeparator());

        StringBuilder builder = new StringBuilder((int) (tokenThreshold * conversationNewOldRatio));
        for (JSONObject message : olderThanRecentConversations) {
            builder.append("-").append(message).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String summary(IAgent agent, String content) throws IOException, InterruptedException {
        String prompt = String.format("""
                    总结这段编码智能体的对话，以便后续工作可以继续推进，仅回答总结内容，不要有过多的阐述。
                    保留以下内容：
                    1. 当前目标
                    2. 重要发现与决策
                    3. 已读取或修改的文件
                    4. 待完成的工作
                    5. 用户的限制条件和偏好
                    6、内容简洁但具体明确
                    对话如下：
                    %s
                """, content);
        AbstractModel model = agent.getModel().cloneWithoutHistory();
        model.addUserMessage(prompt);
        return model.chat().getJSONObject("message").getString("content");
    }
}
