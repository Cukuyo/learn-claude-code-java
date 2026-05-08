package org.example.define_agent.core;

import com.alibaba.fastjson2.JSONObject;

import org.example.define_agent.AgentHook;
import org.example.define_agent.core.components.SkillUseComponent;
import org.example.define_agent.core.components.ToolUseComponent;
import org.example.define_models.AbstractModel;

import java.io.IOException;

/**
 * agent核心类:
 * 提供agentLoop实现，即tool use循环
 * 提供skillUse组件
 */
public class AgentLoopAgent extends AbstractAgent {
    protected ToolUseComponent toolUseComponent;
    protected SkillUseComponent skillUseAgent;

    public AgentLoopAgent(AbstractModel model, String agentName) {
        super(model, agentName);

        model.addSystemMessages(
            "你当前的工作目录是<" + System.getProperty("user.dir") + ">，注意不要做出范围之外的危险行为！");

        toolUseComponent = new ToolUseComponent(this);
        skillUseAgent = new SkillUseComponent(this);
    }

    @Override
    protected String agentLoop(String content) throws IOException, InterruptedException {
        // 添加User提示词后回调
        callAfterAddUserMessage(this, addUserMessageWithOptionHook(content));

        while (true) {
            // chat前回调
            callBeforeChat(this);

            JSONObject chatRsp = getChatRspWithOptionHook();
            JSONObject message = chatRsp.getJSONObject("message");
            model.addAssistantMessages(message);

            // chat后回调
            callAfterChat(this, chatRsp, message);

            // 非工具调用即刻返回
            if (!chatRsp.getString("finish_reason").equals("tool_calls")) {
                return message.getString("content");
            }

            // 工具使用前回调
            callBeforeToolsUse(this);
            // 依次调用tools
            message.getJSONArray("tool_calls").forEach(obj -> toolUseComponent.toolUse((JSONObject) obj));
            // 工具使用后回调
            callAfterToolsUse(this);
        }
    }

    protected JSONObject getChatRspWithOptionHook() throws IOException, InterruptedException {
        JSONObject chatRsp = null;
        for (AgentHook agentHook : agentHooks) {
            chatRsp = agentHook.hookChat(this);
            if (chatRsp != null) {
                break;
            }
        }
        if (chatRsp == null) {
            chatRsp = model.chat();
        }
        return chatRsp;
    }

    protected JSONObject addUserMessageWithOptionHook(String content) {
        JSONObject userMessage = null;
        for (AgentHook agentHook : agentHooks) {
            userMessage = agentHook.hookAddUserMessage(this, content);
            if (userMessage != null) {
                break;
            }
        }
        if (userMessage == null) {
            userMessage = model.addUserMessage(content);
        }
        return userMessage;
    }

    @Override
    public void registryTool(Object toolObj) {
        toolUseComponent.registryTool(toolObj);
    }

    @Override
    public void registryTool(Class<?> toolObj) {
        toolUseComponent.registryTool(toolObj);
    }

    @Override
    public void registrySkills(String dirPath) {
        skillUseAgent.registrySkills(dirPath);
    }
}
