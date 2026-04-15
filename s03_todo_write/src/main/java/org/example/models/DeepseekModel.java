package org.example.models;

import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * Deepseek客户端
 */
public class DeepseekModel extends AbstractModel {
    private final String url;
    private final String apiKey;
    private final String model;

    public DeepseekModel(String apiKey) {
        this("https://api.deepseek.com/chat/completions", apiKey);
    }

    public DeepseekModel(String url, String apiKey) {
        this("deepseek-chat", url, apiKey);
    }

    public DeepseekModel(String model, String url, String apiKey) {
        super();
        this.url = url;
        this.apiKey = "Bearer " + apiKey;
        this.model = model;
        curReq.put("model", model);
        curReq.put("frequency_penalty", 0);
        curReq.put("max_tokens", 4096);
        curReq.put("presence_penalty", 0);
        curReq.put("top_p", 1);
    }

    /**
     * 示例：
     * {
     * "type": "object",
     * "properties": {
     * "keywords": {
     * "type": "array",
     * "description": "Five keywords of the article, sorted by importance",
     * "items": {
     * "type": "string",
     * "description": "A concise and accurate keyword or phrase."
     * }
     * }
     * },
     * "required": ["keywords"],
     * "additionalProperties": false
     * }
     *
     * @param function function
     * @return tool json
     */
    @Override
    public JSONObject buildTool(JSONObject function) {
        JSONObject tool = new JSONObject();
        tool.put("type", "function");
        tool.put("function", function);
        return tool;
    }

    @Override
    public JSONObject buildToolFunction(String name, String desc, JSONObject parameters) {
        JSONObject function = new JSONObject();
        function.put("name", name);
        function.put("description", desc);
        function.put("parameters", parameters);
        return function;
    }

    @Override
    public JSONObject buildToolParameters(JSONObject properties, String[] required) {
        JSONObject function = new JSONObject();
        function.put("type", "object");
        function.put("properties", properties);
        function.put("required", required);
        return function;
    }

    @Override
    public JSONObject buildToolProperties(Map<String, JSONObject> properties) {
        return new JSONObject(properties);
    }

    @Override
    public JSONObject buildToolProperty(String type, String description, Object[] enums, JSONObject items) {
        JSONObject property = new JSONObject();
        property.put("type", type);
        property.put("description", description);
        property.put("enums", enums);
        property.put("items", items);
        return property;
    }

    @Override
    public JSONObject chat() throws IOException, InterruptedException {
        return super.chat().getJSONArray("choices").getJSONObject(0);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getModel() {
        return model;
    }

}
