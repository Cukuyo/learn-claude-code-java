package org.example.tool;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * ToolExecuter构建工具类
 */
public class ToolExecuterBuildUtil {
    /**
     * 构建ToolExecuter
     *
     * @param invokeObj 执行bean。static方法为null
     * @param method    method
     * @return ToolExecuter
     */
    public static ToolExecuter buildToolExecuter(Object invokeObj, Method method) {
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
                    invokeArgs[i] = ToolParamConvertUtil.convert(value, param.getType());
                }

                return (String) method.invoke(invokeObj, invokeArgs);
            } catch (Exception e) {
                return "执行失败：" + e.getMessage();
            }
        };
    }
}
