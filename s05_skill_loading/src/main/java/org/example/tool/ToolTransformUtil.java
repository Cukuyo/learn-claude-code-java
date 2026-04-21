package org.example.tool;

import com.alibaba.fastjson2.JSONObject;
import org.example.models.AbstractModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将tool工具解析转换为model需要格式的工具类
 */
public class ToolTransformUtil {
    /**
     * tool工具解析转换为model需要的json格式
     *
     * @param toolResolveResult 解析结果
     * @param model             模型
     * @return 模型所需的json格式
     */
    public static JSONObject transform(ToolResolveUtil.ToolResolveResult toolResolveResult, AbstractModel model) {
        // 迭代解析字段
        Map<String, JSONObject> map = new HashMap<>();
        List<String> required = new ArrayList<>();
        for (ToolResolveUtil.ToolResolveItem toolResolveItem : toolResolveResult.properties()) {
            map.put(toolResolveResult.name(), buildToolProperty(toolResolveItem, model));
            if (toolResolveItem.required()) {
                required.add(toolResolveItem.name());
            }
        }

        // 从下往上组装
        JSONObject properties = model.buildToolProperties(map);
        JSONObject parameters = model.buildToolParameters(properties, required.toArray(new String[0]));
        JSONObject function = model.buildToolFunction(toolResolveResult.name(), toolResolveResult.description(), parameters);
        return model.buildTool(function);
    }

    private static JSONObject buildToolProperty(ToolResolveUtil.ToolResolveItem toolResolveItem, AbstractModel model) {
        // 迭代解析字段
        Map<String, JSONObject> map = new HashMap<>();
        for (ToolResolveUtil.ToolResolveItem item : toolResolveItem.properties()) {
            map.put(item.name(), buildToolProperty(item, model));
        }

        JSONObject properties;
        // 原逻辑不好处理基础类型数组的组装，相当于特例了，于是在次进行单独判断
        if (toolResolveItem.type().equals("array") && toolResolveItem.properties().getFirst().name().isEmpty()) {
            ToolResolveUtil.ToolResolveItem first = toolResolveItem.properties().getFirst();
            properties = model.buildToolProperty(first.type(), first.description(), toolResolveItem.enums(), null);
        } else {
            properties = model.buildToolProperties(map);
        }

        return model.buildToolProperty(toolResolveItem.type(), toolResolveItem.description(), toolResolveItem.enums(), properties);
    }
}
