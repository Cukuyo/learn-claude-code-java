package org.example.agent.common;

import com.alibaba.fastjson2.JSONObject;
import org.example.models.AbstractModel;
import org.example.tool.ToolExecuter;
import org.example.tool.ToolResolveUtil;
import org.example.tool.ToolTransformUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * agent抽象父类，提供公共方法，定义架构
 */
public class ToolUseAgent extends BaseAgent {
    protected final Map<String, ToolExecuter> toolHandlers = new HashMap<>();

    public ToolUseAgent(AbstractModel model, String agentName) {
        super(model, agentName);
    }

    @Override
    protected void toolUse(JSONObject obj) {
        JSONObject function = obj.getJSONObject("function");

        // tool参数
        String id = obj.getString("id");
        String name = function.getString("name");
        JSONObject arguments = JSONObject.parse(function.getString("arguments"));

        // 工具使用前回调
        callBeforeToolUse(id, name, arguments);

        String toolRsp = toolHandlers.get(name).execute(arguments);
        model.addToolMessages(toolRsp, id);

        // 工具使用后回调
        callAfterToolUse(id, name, arguments, toolRsp);
    }

    /**
     * 单个工具使用后回调
     *
     * @param id        tool id
     * @param name      tool name
     * @param arguments tool args
     */
    protected void callBeforeToolUse(String id, String name, JSONObject arguments) {
        System.out.printf("<%s> 开始执行tool, id:%s, func:%s, args:%s %s", agentName, id, name, arguments, System.lineSeparator());
    }

    /**
     * 单个工具使用前回调
     *
     * @param id        tool id
     * @param name      tool name
     * @param arguments tool args
     * @param toolRsp   toolRsp
     */
    protected void callAfterToolUse(String id, String name, JSONObject arguments, String toolRsp) {
        System.out.printf("<%s> 结束执行tool, id:%s, func:%s, args:%s , result:%s %s", agentName, id, name, arguments, toolRsp, System.lineSeparator());
    }

    /**
     * 工具注册
     *
     * @param toolObj 实例类型tool
     */
    public void registryTool(Object toolObj) {
        registryTool(ToolResolveUtil.resolve(toolObj));
    }

    /**
     * 工具注册
     *
     * @param toolObj 静态方法类型tool
     */
    public void registryTool(Class<?> toolObj) {
        registryTool(ToolResolveUtil.resolve(toolObj));
    }

    private void registryTool(List<ToolResolveUtil.ToolResolveResult> toolResolveResults) {
        for (ToolResolveUtil.ToolResolveResult toolResolveResult : toolResolveResults) {
            toolHandlers.put(toolResolveResult.name(), toolResolveResult.toolHandler());
            model.addTool(ToolTransformUtil.transform(toolResolveResult, model));
        }
    }
}
