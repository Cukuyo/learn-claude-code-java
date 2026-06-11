package org.example.framework_agent.core;

import com.alibaba.fastjson2.JSONObject;

import org.example.framework_agent.AgentHook;
import org.example.framework_agent.core.components.SkillUseComponent;
import org.example.framework_agent.core.components.ToolUseComponent;
import org.example.framework_models.AbstractModel;

import java.io.IOException;

/**
 * agent核心类:
 * 提供agentLoop实现，即tool use循环
 * 提供skillUse组件
 */
public class AgentLoopAgent extends AbstractAgent {
    protected ToolUseComponent toolUseComponent;
    protected SkillUseComponent skillUseAgent;

    public AgentLoopAgent(AbstractModel model, String agentName, String role) {
        super(model, agentName, role);

        model.addSystemMessages(
                "当前的工作目录是<" + System.getProperty("user.dir") + ">" +
                        "当前的操作系统是<" + System.getProperty("os.name") + ">" +
                        "，注意不要做出范围之外的危险行为！");

        toolUseComponent = new ToolUseComponent(this);
        skillUseAgent = new SkillUseComponent(this);
    }

    @Override
    protected String agentLoop() throws IOException, InterruptedException {
        // ai返回拼接
        StringBuilder resultBuilder = new StringBuilder(model.getMaxTokens() / 2);
        // 遇到错误时的最大重试次数
        int retryMaxTime = 3;

        agent_loop:
        while (true) {
            // chat前回调
            callBeforeChat(this);

            JSONObject chatRsp = getChatRspWithOptionHook();
            JSONObject message = chatRsp.getJSONObject("message");

            model.addAssistantMessages(message);

            String rspContent = message.getString("content");
            String finishReason = chatRsp.getString("finish_reason");
            switch (finishReason) {
                // 模型自然停止生成，或遇到 stop 序列中列出的字符串
                case "stop":
                    callAfterChat(this, chatRsp, message, true);

                    resultBuilder.append(rspContent);
                    break agent_loop;

                // 输出内容因触发过滤策略而被过滤。
                case "content_filter":
                    callAfterChat(this, chatRsp, message, true);

                    resultBuilder.append(rspContent).append("......").append("输出内容因触发过滤策略而被过滤");
                    break agent_loop;

                // 系统推理资源不足，生成被打断。         
                case "insufficient_system_resource":
                    if (retryMaxTime > 0) {
                        callAfterChat(this, chatRsp, message, false);

                        retryMaxTime--;
                        resultBuilder.append(rspContent);
                        model.addUserMessage("系统推理资源不足，生成被打断。直接从中断处继续，无需回顾总结、不重复内容，必要时可从句子中间接续行文");
                        break;
                    } else {
                        callAfterChat(this, chatRsp, message, true);

                        resultBuilder.append("......").append("系统推理资源不足，生成被打断");
                        break agent_loop;
                    }

                    // 输出长度达到了模型上下文长度限制，或达到了 max_tokens 的限制。
                case "length":
                    callAfterChat(this, chatRsp, message, false);

                    resultBuilder.append(rspContent);
                    model.addUserMessage("已达到输出上限。直接从中断处继续，无需回顾总结、不重复内容，必要时可从句子中间接续行文");
                    break;

                // 工具使用
                case "tool_calls":
                    callAfterChat(this, chatRsp, message, false);

                    // 工具使用前回调
                    callBeforeToolsUse(this);
                    // 依次调用tools
                    message.getJSONArray("tool_calls").forEach(obj -> toolUseComponent.toolUse((JSONObject) obj));
                    // 工具使用后回调
                    callAfterToolsUse(this);
                    break;

                default:
                    callAfterChat(this, chatRsp, message, true);

                    resultBuilder.append(rspContent).append("......").append("未知的finish_reason: ").append(finishReason);
                    break agent_loop;
            }
        }

        return resultBuilder.toString();
    }

    protected JSONObject getChatRspWithOptionHook() throws IOException, InterruptedException {
        JSONObject chatRsp = null;
        for (AgentHook agentHook : agentHooks) {
            chatRsp = agentHook.hookChat(this, model.getMessages());
            if (chatRsp != null) {
                break;
            }
        }
        if (chatRsp == null) {
            chatRsp = model.chat();
        }
        return chatRsp;
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
