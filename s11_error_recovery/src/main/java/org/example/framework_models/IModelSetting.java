package org.example.framework_models;

/**
 * 定义模型的参数配置
 */
public interface IModelSetting {
    String getUrl();

    void setUrl(String url);

    String getApiKey();

    void setApiKey(String apiKey);

    String getModel();

    String setModel(String model);

    int getMaxInputTokens();

    int getMaxOutTokens();

    int getMaxTokens();

    void setMaxTokens();

    boolean isEnabledThink();

    void setEnabledThink(boolean isEnabledThink);

    double getTemperature();

    void setTemperature(double temperature);

    int getTopP();

    void setTopP();
}
