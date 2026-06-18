package org.example.models;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

/**
 * 定义工具的构造
 */
public interface IModelToolUse {
    JSONArray getTools();

    void setTools(JSONArray tools);

    void addTool(JSONObject tool);

    JSONObject getTool(String toolName);

    void removeTool(String toolName);

    String extractToolName(JSONObject tool);

    JSONObject buildTool(JSONObject function);

    JSONObject buildToolFunction(String name, String desc, JSONObject parameters);

    JSONObject buildToolParameters(JSONObject properties, String[] required);

    JSONObject buildToolProperties(Map<String, JSONObject> properties);

    JSONObject buildToolProperty(String type, String description, Object[] enums, JSONObject items);
}
