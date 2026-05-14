package org.example.framework_models;

import com.alibaba.fastjson2.JSONArray;

/**
 * 定义模型的参数配置
 */
public interface IModelSetting {
    String getUrl();

    void setUrl(String url);

    String getApiKey();

    void setApiKey(String apiKey);

    String getModel();

    void setModel(String model);

    int getMaxInputTokens();

    int getMaxOutTokens();

    int getMaxTokens();

    void setMaxTokens(int maxTokens);

    boolean isEnabledThink();

    void setEnabledThink(boolean isEnabledThink);

    double getTemperature();

    void setTemperature(double temperature);

    int getTopP();

    void setTopP(int topP);

    String getResponseFormat();

    void setResponseFormat(String format);

    String getResoningEffort();

    void setResoningEffort(String effort);

    JSONArray getMessages();

    void setMessages(JSONArray messages);
}
