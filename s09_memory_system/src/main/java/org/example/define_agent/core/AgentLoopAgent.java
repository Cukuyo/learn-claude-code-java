package org.example.define_agent.core;

import com.alibaba.fastjson2.JSONObject;

import org.example.define_agent.AgentHook;
import org.example.define_agent.core.components.SkillUseComponent;
import org.example.define_tool.ToolExecuter;
import org.example.define_tool.ToolResolveUtil;
import org.example.define_tool.ToolTransformUtil;
import org.example.models.AbstractModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * agent核心类:
 * 提供agentLoop实现
 */
public class AgentLoopAgent extends AbstractAgent {
    protected final Map<String, ToolExecuter> toolHandlers = new HashMap<>();

    protected SkillUseComponent skillUseAgent;

    public AgentLoopAgent(AbstractModel model, String agentName) {
        super(model, agentName);
         model.addSystemMessages("[ToolUse]你当前的工作目录是<" + System.getProperty("user.dir") + ">，执行tools时注意不要做出范围之外的危险行为！");
        skillUseAgent=new SkillUseComponent(this);
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
            message.getJSONArray("tool_calls").forEach(obj -> toolUse((JSONObject) obj));
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

    protected void toolUse(JSONObject obj) {
        JSONObject function = obj.getJSONObject("function");

        // tool参数
        String id = obj.getString("id");
        String name = function.getString("name");
        JSONObject arguments = JSONObject.parse(function.getString("arguments"));

        // 工具使用前回调
        callBeforeToolUse(this, id, name, arguments);

        JSONObject toolMessage = model.addToolMessage(getToolRspWithOptionHook(id, name, arguments), id);

        // 工具使用后回调
        callAfterToolUse(this, id, name, arguments, toolMessage);
    }

    protected String getToolRspWithOptionHook(String id, String name, JSONObject arguments) {
        String toolRsp = null;
        for (AgentHook agentHook : agentHooks) {
            toolRsp = agentHook.hookToolUse(this, id, name, arguments);
            if (toolRsp != null) {
                break;
            }
        }
        if (toolRsp == null) {
            toolRsp = toolHandlers.get(name).execute(arguments);
        }
        return toolRsp;
    }

    /**
     * 工具注册
     *
     * @param toolObj 实例类型tool
     */
    @Override
    public void registryTool(Object toolObj) {
        registryTool(ToolResolveUtil.resolve(toolObj));
    }

    /**
     * 工具注册
     *
     * @param toolObj 静态方法类型tool
     */
    @Override
    public void registryTool(Class<?> toolObj) {
        registryTool(ToolResolveUtil.resolve(toolObj));
    }

    private void registryTool(List<ToolResolveUtil.ToolResolveResult> toolResolveResults) {
        for (ToolResolveUtil.ToolResolveResult toolResolveResult : toolResolveResults) {
            toolHandlers.put(toolResolveResult.name(), toolResolveResult.toolHandler());
            model.addTool(ToolTransformUtil.transform(toolResolveResult, model));
        }
    }

    @Override
    public void registrySkills(String dirPath) {
        skillUseAgent.registrySkills(dirPath);
    }
}
