package org.example.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

/**
 * JsonClone工具
 */
public class JsonCloneUtil {
    /**
     * 深度克隆 JSONObject，包括：
     * 1. value 完全深克隆
     * 2. key 如果是 JSONObject/JSONArray，也会深克隆
     *
     * @param original JSONObject
     * @return JSONObject
     */
    public static JSONObject deepClone(JSONObject original) {
        if (original == null) {
            return null;
        }

        JSONObject cloned = new JSONObject();

        for (Map.Entry<String, Object> entry : original.entrySet()) {
            cloned.put(entry.getKey(), deepCloneValue(entry.getValue()));
        }

        return cloned;
    }

    /**
     * 递归克隆任意 JSON 类型
     *
     * @param value val
     * @return Object
     */
    private static Object deepCloneValue(Object value) {
        switch (value) {
            case null -> {
                return null;
            }
            case JSONObject jsonObject -> {
                return deepClone(jsonObject);
            }
            case JSONArray array -> {
                JSONArray newArray = new JSONArray(array.size());
                for (Object item : array) {
                    newArray.add(deepCloneValue(item));
                }
                return newArray;
            }
            default -> {
                // 基本类型、字符串直接返回（不可变）
                return value;
            }
        }
    }
}
