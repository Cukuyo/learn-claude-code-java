package org.example.agent.tool;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.Future;

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
                        return ToolExecuter.simpleRsp("缺失必选参数：" + paramName);
                    }

                    // 转换为java tool定义的类型
                    invokeArgs[i] = ToolParamConvertUtil.convert(value, param.getType());
                }
                Object invokeRsp = method.invoke(invokeObj, invokeArgs);
                if (invokeRsp.getClass().isPrimitive()) {
                    return ToolExecuter.simpleRsp(String.valueOf(invokeRsp));
                }
                if (invokeRsp.getClass().equals(String.class)) {
                    return ToolExecuter.simpleRsp((String) invokeRsp);
                }
                if (invokeRsp instanceof Future<?>) {
                    return (Future<String>) invokeRsp;
                }

                return ToolExecuter.simpleRsp((String.valueOf(invokeRsp)));
            } catch (Exception e) {
                return ToolExecuter.simpleRsp("执行失败：" + e.getMessage());
            }
        };
    }
}
