package org.example.models;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.utils.HttpClientUtil;

import java.io.IOException;
import java.util.Map;

/**
 * model抽象父类，提供公共方法
 */
public abstract class AbstractModel {
    public final JSONObject curReq = new JSONObject();

    public AbstractModel() {
        curReq.put("messages", new JSONArray());
        curReq.put("tools", new JSONArray());
    }

    public abstract String getUrl();

    public abstract String getApiKey();

    public abstract String getModel();

    public abstract JSONObject buildTool(JSONObject function);

    public abstract JSONObject buildToolFunction(String name, String desc, JSONObject parameters);

    public abstract JSONObject buildToolParameters(JSONObject properties, String[] required);

    public abstract JSONObject buildToolProperties(Map<String, JSONObject> properties);

    public abstract JSONObject buildToolProperty(String type, String description, Object[] enums, JSONObject items);

    /**
     * 使用当前提示词请求一次
     *
     * @return 请求响应
     * @throws IOException          io异常
     * @throws InterruptedException 线程等待中断
     */
    public JSONObject chat() throws IOException, InterruptedException {
        JSONObject result = HttpClientUtil.send(getUrl(), getApiKey(), curReq);

        System.out.println(result);
        JSONObject usage = result.getJSONObject("usage");
        System.out.printf("请求url:%s, 模型:%s, 提示词token数:%d, 补全token数:%d, 总token数:%d %s", getUrl(), getModel(), usage.getInteger("prompt_tokens"), usage.getInteger("completion_tokens"), usage.getInteger("total_tokens"), System.lineSeparator());

        return result;
    }

    /**
     * 添加tool
     *
     * @param tool tool
     */
    public void addTool(JSONObject tool) {
        ((JSONArray) curReq.get("tools")).add(tool);
    }

    /**
     * 构造系统提示词
     *
     * @param content content
     */
    public void addSystemMessages(String content) {
        ((JSONArray) curReq.get("messages")).add(message(content, "system"));
    }

    /**
     * 构造用户提示词
     *
     * @param content content
     */
    public void addUserMessage(String content) {
        ((JSONArray) curReq.get("messages")).add(message(content, "user"));
    }

    /**
     * 构造工具提示词
     *
     * @param content    content
     * @param toolCallId toolCallId
     */
    public void addToolMessages(String content, String toolCallId) {
        JSONObject msg = message(content, "tool");
        msg.put("tool_call_id", toolCallId);
        ((JSONArray) curReq.get("messages")).add(msg);
    }

    /**
     * 传入模型返回的助手提示词
     *
     * @param content content
     */
    public void addAssistantMessages(JSONObject content) {
        ((JSONArray) curReq.get("messages")).add(content);
    }

    private JSONObject message(String content, String role) {
        JSONObject msg = new JSONObject();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }
}
