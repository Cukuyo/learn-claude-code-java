package org.example.agent.tool.impl;

import com.alibaba.fastjson2.JSONObject;

import org.example.agent.AgentHook;
import org.example.agent.IAgentToolUse;
import org.example.agent.impl.AbstractAgent;
import org.example.agent.tool.ToolExecuter;
import org.example.agent.tool.ToolResolveUtil;
import org.example.agent.tool.ToolTransformUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * agent父类：
 * 提供skillUse实现
 */
public class ToolUseComponent implements IAgentToolUse {
    protected AbstractAgent agent;

    protected final Map<String, ToolExecuter> toolHandlers = new HashMap<>();

    public ToolUseComponent(AbstractAgent agent) {
        this.agent = agent;
        registryTool(this);

        this.agent.getModel().addSystemMessages("[ToolUse]你可以使用各种tools完成复杂工作，但注意高危命令的使用和多步骤的规划！");
    }

    public void toolUse(JSONObject obj) {
        JSONObject function = obj.getJSONObject("function");

        // tool参数
        String id = obj.getString("id");
        String name = function.getString("name");
        JSONObject arguments = JSONObject.parse(function.getString("arguments"));

        // 工具使用前回调
        agent.callBeforeToolUse(agent, id, name, arguments);

        JSONObject toolMessage = agent.getModel().addToolMessage(getToolRspWithOptionHook(id, name, arguments), id);

        // 工具使用后回调
        agent.callAfterToolUse(agent, id, name, arguments, toolMessage);
    }

    protected String getToolRspWithOptionHook(String id, String name, JSONObject arguments) {
        String toolRsp = null;
        for (AgentHook agentHook : agent.agentHooks) {
            toolRsp = agentHook.hookToolUse(agent, id, name, arguments);
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
            agent.getModel().addTool(ToolTransformUtil.transform(toolResolveResult, agent.model));
        }
    }
}
