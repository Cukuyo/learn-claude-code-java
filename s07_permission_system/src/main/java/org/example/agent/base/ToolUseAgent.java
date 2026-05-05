package org.example.agent.base;

import com.alibaba.fastjson2.JSONObject;
import org.example.models.AbstractModel;
import org.example.use_tool.ToolExecuter;
import org.example.use_tool.ToolResolveUtil;
import org.example.use_tool.ToolTransformUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * agent抽象父类:
 * 提供toolUse的实现
 */
public abstract class ToolUseAgent extends AgentLoopAgent {
    protected final Map<String, ToolExecuter> toolHandlers = new HashMap<>();

    public ToolUseAgent(AbstractModel model, String agentName) {
        super(model, agentName);
        model.addSystemMessages("你当前的工作目录是<" + System.getProperty("user.dir") + ">，执行tools时注意不要做出范围之外的危险行为！");
    }

    @Override
    protected void toolUse(JSONObject obj) {
        JSONObject function = obj.getJSONObject("function");

        // tool参数
        String id = obj.getString("id");
        String name = function.getString("name");
        JSONObject arguments = JSONObject.parse(function.getString("arguments"));

        // 工具使用前回调
        callBeforeToolUse(this, id, name, arguments);

        String toolRsp = toolHandlers.get(name).execute(arguments);
        JSONObject toolMessage = model.addToolMessage(toolRsp, id);

        // 工具使用后回调
        callAfterToolUse(this, id, name, arguments, toolMessage);
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
}
