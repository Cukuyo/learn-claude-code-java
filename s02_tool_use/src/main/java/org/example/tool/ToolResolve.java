package org.example.tool;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class ToolResolve {

    public static List<ToolResolveResult> resolve(Object obj){
        List<ToolResolveResult> result = new ArrayList<>();

        for (Method method : getAnnotationedMethods(obj)) {
            String toolName=method.getName();
            String desc=method.getAnnotation(ToolMethod.class).description();

            Parameter[] params = method.getParameters();
            String[][] properties=new String[params.length][];
            for (int i = 0; i < params.length; i++) {
                    Parameter param = params[i];
                    ToolParam paramAnno = param.getAnnotation(ToolParam.class);

                    String[] property=new String[4];
                    property[0]=param.getName();
                    property[1]=param.getType().getTypeName();
                    property[2]=paramAnno.description();
                    property[30]=paramAnno.required()+"";
            }

            result.add(new ToolResolveResult(toolName,desc,properties,buildToolExcuter(obj, method)));    
        }


        return result;
    }

    private static List<Method> getAnnotationedMethods(Object obj){
        List<Method> result = new ArrayList<>();

        for (Method method : obj.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(ToolMethod.class) == null) continue;
            result.add(method);
        }

        return result;
    }


    private static ToolExcuter buildToolExcuter(Object obj,Method method) {
        return args -> {
            try {
                method.setAccessible(true);
                
                Parameter[] params = method.getParameters();
                Object[] invokeArgs = new Object[params.length];                

                for (int i = 0; i < params.length; i++) {
                    Parameter param = params[i];
                    ToolParam paramAnno = param.getAnnotation(ToolParam.class);

                    String paramName = param.getName();
                    Object value = args.get(paramName);

                    if (paramAnno.required() && value == null) {
                        return "缺失必选参数：" + paramName;
                    }
                    invokeArgs[i] = convert(value, param.getType());
                }

                return (String) method.invoke(obj, invokeArgs);
            } catch (Exception e) {
                return "执行失败：" + e.getMessage();
            }
        };
    }

    /**
     * 类型转换：将 LLM 传入的参数值 转为 Java 方法需要的类型
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
            "不支持的参数类型转换: 无法将 " + value.getClass().getName()
            + " 转为 " + targetType.getName()
        );
    }
}
