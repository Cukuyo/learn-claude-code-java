package org.example.tool;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

public class ToolResolve {
    /**
     * 解析后tool的参数信息
     *
     * @param name        tool参数名
     * @param type        tool参数类型
     * @param enums        tool参数如果是enum，该值为枚举可能的值
     * @param description 描述
     * @param required  tool参数是否为必选
     * @param properties tool参数如果不是基础类型，会继续往下迭代
     */
    public record ToolResolveItem(String name, String type, Object[] enums, String description, boolean required, List<ToolResolveItem> properties) {
    }

    /**
     * 解析后tool的信息
     *
     * @param name        tool名
     * @param description 描述
     * @param properties  参数属性
     * @param toolHandler 执行体
     */
    public record ToolResolveResult(String name, String description, List<ToolResolveItem> properties, ToolExecuter toolHandler) {
    }

    /**
     * 从给定类里解析可使用的tools
     *
     * @param obj 类对象
     * @return 类里面可使用的tools
     */
    public static List<ToolResolveResult> resolve(Object obj) {
        return getAnnotatedMethods(obj.getClass()).stream().map(method -> getToolResolveResult(obj, method)).toList();
    }

    /**
     * 从给定类里解析可使用的tools
     *
     * @param obj 类对象
     * @return 类里面可使用的tools
     */
    public static List<ToolResolveResult> resolve(Class<?> obj) {
        return getAnnotatedMethods(obj).stream().map(method -> getToolResolveResult(null, method)).toList();
    }

    private static ToolResolveResult getToolResolveResult(Object invokeObj, Method method) {
        String name = method.getName();
        String desc = method.getAnnotation(ToolMethod.class).description();
        List<ToolResolveItem> properties = Stream.of( method.getParameters()).map(p->getToolResolveItem(p)).toList();

        return new ToolResolveResult(name, desc, properties, buildToolExecuter(invokeObj, method));
    }

    private static ToolResolveItem getToolResolveItem(Parameter parameter){
        String name = parameter.getName();
        String type = parameter.getType().getSimpleName().toLowerCase();
        Object[] enums = parameter.getType().isEnum()?parameter.getType().getEnumConstants():null;

        ToolParam paramAnno = parameter.getAnnotation(ToolParam.class);
        String description = paramAnno.description();
        boolean required = paramAnno.required();
        List<ToolResolveItem> properties = paramAnno.baseClass()?null:getAnnotatedFields(parameter.getClass()).stream().map(f->getToolResolveItem(f)).toList();
        
        return new ToolResolveItem(name, type, enums, description, required, properties);
    }

    private static ToolResolveItem getToolResolveItem(Field parameter){
        String name = parameter.getName();
        String type = parameter.getType().getSimpleName().toLowerCase();
        Object[] enums = parameter.getType().isEnum()?parameter.getType().getEnumConstants():null;

        ToolParam paramAnno = parameter.getAnnotation(ToolParam.class);
        String description = paramAnno.description();
        boolean required = paramAnno.required();
        List<ToolResolveItem> properties = paramAnno.baseClass()?null:getAnnotatedFields(parameter.getClass()).stream().map(f->getToolResolveItem(f)).toList();
        
        return new ToolResolveItem(name, type, enums, description, required, properties);
    }

    private static List<Method> getAnnotatedMethods(Class<?> obj) {
        return Arrays.stream(obj.getDeclaredMethods()).filter(ToolResolve::checkMethod).toList();
    }

    private static boolean checkMethod(Method method) {
        return method.getAnnotation(ToolMethod.class) != null;
    }

    private static List<Field> getAnnotatedFields(Class<?> obj) {
        return Arrays.stream(obj.getDeclaredFields()).filter(ToolResolve::checkFields).toList();
    }

    private static boolean checkFields(Field field) {
        return field.getAnnotation(ToolParam.class) != null;
    }

    private static ToolExecuter buildToolExecuter(Object invokeObj, Method method) {
        return args -> {
            try {
                method.setAccessible(true);

                Parameter[] params = method.getParameters();
                Object[] invokeArgs = new Object[params.length];

                for (int i = 0; i < params.length; i++) {
                    Parameter param = params[i];
                    String paramName = param.getName();
                    Object value = args.get(paramName);

                    // 判断必须参数是否赋值
                    ToolParam paramAnno = param.getAnnotation(ToolParam.class);
                    if (paramAnno.required() && value == null) {
                        return "缺失必选参数：" + paramName;
                    }

                    // 转换为java tool定义的类型
                    invokeArgs[i] = convert(value, param.getType());
                }

                return (String) method.invoke(invokeObj, invokeArgs);
            } catch (Exception e) {
                return "执行失败：" + e.getMessage();
            }
        };
    }

    /**
     * 类型转换：将 LLM 传入的参数值 转为 Java 方法需要的类型
     *
     * @param value      待转换值
     * @param targetType 目标类型
     * @return 转换后值
     */
    private static Object convert(Object value, Class<?> targetType) {
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
            return (Boolean) value;
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
        throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() +" 转为 " + targetType.getSimpleName());
    }
}
