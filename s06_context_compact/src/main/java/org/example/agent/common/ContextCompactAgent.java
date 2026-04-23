package org.example.agent.common;

import org.example.models.AbstractModel;

/**
 * agent抽象父类，提供公共方法，定义架构
 */
public class ContextCompactAgent extends SkillUseAgent {
    public ContextCompactAgent(AbstractModel model, String agentName) {
        super(model, agentName);
    }

    @Override
    protected void callBeforeChat() {
        // 微压缩
        // 会话总结
        super.callBeforeChat();
    }
}
