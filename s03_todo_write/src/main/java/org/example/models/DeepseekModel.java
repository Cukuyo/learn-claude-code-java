package org.example.models;

import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;

import org.example.tool.ToolResolve;

/**
 * Deepseek客户端
 */
public class DeepseekModel extends AbstractModel {
    private final String url;
    private final String apiKey;

    public DeepseekModel(String apiKey) {
        this("https://api.deepseek.com/chat/completions", apiKey);
    }

    public DeepseekModel(String url, String apiKey) {
        this("deepseek-chat", url, apiKey);
    }

    public DeepseekModel(String model, String url, String apiKey) {
        super(model);
        this.url = url;
        this.apiKey = "Bearer " + apiKey;
    }

    public void addToolResolveResult(ToolResolve.ToolResolveResult toolResolveResult){
        // toolResolveResult.properties()
        // buildFunction(url, apiKey, null);

    }

    @Override
    public JSONObject buildFunction(String name, String desc, JSONObject parameters) {
        JSONObject function = new JSONObject();
        function.put("name", name);
        function.put("description", desc);
        function.put("parameters", parameters);
        return function;
    }

    @Override
    public JSONObject buildParameters(JSONObject properties, String[] required) {
        JSONObject function = new JSONObject();
        function.put("type", "object");
        function.put("properties", properties);
        function.put("required", required);
        return function;
    }

    @Override
    public JSONObject buildParameter(String type, String description, Object[] enums) {
        JSONObject function = new JSONObject();
        function.put("type", type);
        function.put("properties", description);
        function.put("enums", enums);
        return function;
    }

    @Override
    public JSONObject buildParameterArray(String type, String description, JSONObject items) {
        JSONObject function = new JSONObject();
        function.put("type", type);
        function.put("properties", description);
        function.put("items", items);
        return function;
    }

    @Override
    public JSONObject chat() throws IOException, InterruptedException {
        return super.chat().getJSONArray("choices").getJSONObject(0);
    }

    @Override
    String getUrl() {
        return url;
    }

    @Override
    String getApiKey() {
        return apiKey;
    }
}
