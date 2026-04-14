package org.example.tool;

import com.alibaba.fastjson2.JSONObject;

@FunctionalInterface
public interface ToolExcuter {
    String execute(JSONObject args);
}

