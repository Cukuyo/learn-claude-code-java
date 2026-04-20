package org.example.tool;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Array;

/**
 * 工具参数转换
 */
public class ToolParamConvertUtil {
    /**
     * 类型转换：将 LLM 传入的参数值 转为 Java 方法需要的类型
     *
     * @param value      待转换值
     * @param targetType 目标类型
     * @return 转换后值
     */
    public static Object convert(Object value, Class<?> targetType) {
        // 1. null 直接返回
        if (value == null) {
            return null;
        }

        // 2. 类型已经匹配，直接返回
        if (targetType.isInstance(value)) {
            return value;
        }

        // 3. 字符串
        if (targetType == String.class) {
            return value.toString();
        }

        // 4. int / Integer
        if (targetType == int.class || targetType == Integer.class) {
            return ((Number) value).intValue();
        }

        // 5. boolean / Boolean
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.valueOf(value.toString());
        }

        // 枚举转换
        if (targetType.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) targetType, value.toString());
        }

        // 数组转换
        if (targetType.isArray()) {
            Class<?> componentType = targetType.getComponentType();
            JSONArray jsonArray = (JSONArray) value;

            Object array = Array.newInstance(componentType, jsonArray.size());
            for (int i = 0; i < jsonArray.size(); i++) {
                Array.set(array, i, convert(jsonArray.get(i), componentType));
            }
            return array;
        }

        // 普通对象转换
        if (!targetType.isPrimitive()) {
            return ((JSONObject) value).toJavaObject(targetType);
        }

        // 不支持的类型
        throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转为 " + targetType.getSimpleName());
    }
}
