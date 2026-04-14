package org.example.tool;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class ToolResolve {
    /**
     * 解析后tool的信息
     *
     * @param name        tool名
     * @param description 描述
     * @param properties  参数属性
     * @param toolHandler 执行体
     */
    public record ToolResolveResult(String name, String description, String[][] properties,
                                    ToolExecuter toolHandler) {
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

        Parameter[] params = method.getParameters();
        String[][] properties = new String[params.length][];
        for (int i = 0; i < params.length; i++) {
            String[] property = new String[4];

            // 解析固有属性
            Parameter param = params[i];
            property[0] = param.getName();
            property[1] = param.getType().getSimpleName().toLowerCase();

            // 解析注解属性
            ToolParam paramAnno = param.getAnnotation(ToolParam.class);
            property[2] = paramAnno.description();
            property[3] = String.valueOf(paramAnno.required());

            properties[i] = property;
        }

        return new ToolResolveResult(name, desc, properties, buildToolExecuter(invokeObj, method));
    }

    private static List<Method> getAnnotatedMethods(Class<?> obj) {
        return Arrays.stream(obj.getDeclaredMethods()).filter(ToolResolve::checkMethod).toList();
    }

    private static boolean checkMethod(Method method) {
        return method.getAnnotation(ToolMethod.class) != null;
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
        // 值为 null 直接返回
        if (value == null) {
            return null;
        }

        // 类型本来就匹配，直接返回
        if (targetType.isInstance(value)) {
            return value;
        }

        String str = value.toString().trim();

        // 字符串类型
        if (targetType == String.class) {
            return str;
        }

        // 整数 int
        if (targetType == int.class || targetType == Integer.class) {
            return str.isEmpty() ? 0 : Integer.parseInt(str);
        }

        // 长整型 long
        if (targetType == long.class || targetType == Long.class) {
            return str.isEmpty() ? 0L : Long.parseLong(str);
        }

        // 双精度浮点 double
        if (targetType == double.class || targetType == Double.class) {
            return str.isEmpty() ? 0.0d : Double.parseDouble(str);
        }

        // 布尔 boolean
        if (targetType == boolean.class || targetType == Boolean.class) {
            return "true".equalsIgnoreCase(str) || "1".equals(str);
        }

        // 浮点 float（极少用）
        if (targetType == float.class || targetType == Float.class) {
            return str.isEmpty() ? 0.0f : Float.parseFloat(str);
        }

        // 无法转换
        throw new IllegalArgumentException(
                "不支持的参数类型转换: 无法将 " + value.getClass().getName() + " 转为 " + targetType.getName()
        );
    }
}
