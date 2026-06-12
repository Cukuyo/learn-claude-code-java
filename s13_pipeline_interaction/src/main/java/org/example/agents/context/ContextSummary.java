package org.example.agents.context;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.agent.AgentCallback;
import org.example.agent.IAgent;
import org.example.agent.impl.AbstractAgent;
import org.example.agents.context.queues.FixedSizeConversationQueue;
import org.example.framework_models.AbstractModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 上下文总结，设置最大上下文阈值x%，最近的会话保留数y。</p>
 * 若当前token数超过模型最大输入token*x%，除最近的y条会话外，其余用户会话将会总结压缩
 */
public class ContextSummary implements AgentCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextSummary.class);

    private final double contextRemainRatio;

    private final FixedSizeConversationQueue recentConversations;
    private final List<JSONObject> olderThanRecentConversations = new LinkedList<>();

    public ContextSummary(double contextRemainRatio, int recentConversations) {
        this.contextRemainRatio = contextRemainRatio;
        this.recentConversations = new FixedSizeConversationQueue(recentConversations);
    }

    @Override
    public void callBeforeAgentLoop(AbstractAgent agent, JSONArray messages, List<JSONObject> userMessageList) {
        for (JSONObject userMessage : userMessageList) {
            olderThanRecentConversations.addAll(recentConversations.add(userMessage, false));
        }
    }

    @Override
    public void callAfterChat(AbstractAgent agent, JSONObject chatRsp, JSONObject assistantMessage, boolean finished) {
        olderThanRecentConversations.addAll(recentConversations.add(assistantMessage, finished));
    }

    @Override
    public void callAfterToolUse(AbstractAgent agent, String id, String name, JSONObject arguments, JSONObject toolMessage) {
        olderThanRecentConversations.addAll(recentConversations.add(toolMessage, false));
    }

    @Override
    public void callBeforeChat(AbstractAgent agent) {
        long totalTokens = agent.getModel().getLastChatTotalTokens();
        double tokenThreshold = agent.getModel().getMaxInputTokens() * contextRemainRatio;
        if (totalTokens <= tokenThreshold) {
            return;
        }

        // 总结会话
        String forSummaryContext = buildSummaryContext(agent, totalTokens, tokenThreshold);
        String summaryContext;
        try {
            summaryContext = summary(agent, forSummaryContext);
            // 一般情况下模型中 token 和字数的换算比例大致如下：
            // 1 个英文字符 ≈ 0.3 个 token。
            // 1 个中文字符 ≈ 0.6 个 token。
            LOGGER.warn("{} 上下文压缩已完成！压缩前预估tokens {}, 压缩后预估tokens {}", agent.getAgentName(),
                    forSummaryContext.length() * 0.6, summaryContext.length() * 0.6);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("上下文压缩失败！", e);
            return;
        }


        JSONArray messages = agent.getModel().getMessages();
        // 清空除系统提示词外的上下文
        messages.removeIf(message -> !((JSONObject) message).getString("role").equals("system"));
        // 先放入压缩的
        String content = String.format("之前的对话已精简压缩，便于智能体继续开展工作。精简后内容如下：%s%s", System.lineSeparator(), summaryContext);
        olderThanRecentConversations.clear();
        olderThanRecentConversations.add(agent.getModel().addUserMessage(content));
        // 再放入最近没压缩过的
        messages.addAll(recentConversations.collect());
    }

    private String buildSummaryContext(AbstractAgent agent, long totalTokens, double tokenThreshold) {
        // 只是按比例简单估算下StringBuilder的容量，防止重复扩容
        double conversationNewOldRatio = 1.0d * olderThanRecentConversations.size() / (olderThanRecentConversations.size() + recentConversations.size());

        LOGGER.warn("{} 触发上下文压缩：model当前tokens数 {}, 阈值 {}, 压缩会话占比 {}",
                agent.getAgentName(), totalTokens, tokenThreshold, conversationNewOldRatio);

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
