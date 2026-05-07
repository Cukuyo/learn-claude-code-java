package org.example.use_tool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * tool解析工具类，支持静态和动态对象方法
 */
public class ToolResolveUtil {
    /**
     * 解析后tool的参数信息
     *
     * @param name        tool参数名
     * @param type        tool参数类型
     * @param enums       tool参数如果是enum，该值为枚举可能的值
     * @param description 描述
     * @param required    tool参数是否为必选
     * @param properties  tool参数如果不是基础类型，会继续往下迭代
     */
    public record ToolResolveItem(String name, String type, Object[] enums, String description, boolean required,
                                  List<ToolResolveItem> properties) {
    }

    /**
     * 解析后tool的信息
     *
     * @param name        tool名
     * @param description 描述
     * @param properties  参数属性
     * @param toolHandler 执行体
     */
    public record ToolResolveResult(String name, String description, List<ToolResolveItem> properties,
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
        List<ToolResolveItem> properties = Stream.of(method.getParameters()).map(ToolResolveUtil::getToolResolveItem).toList();

        return new ToolResolveResult(name, desc, properties, ToolExecuterBuildUtil.buildToolExecuter(invokeObj, method));
    }

    private static ToolResolveItem getToolResolveItem(Parameter parameter) {
        String name = parameter.getName();
        Class<?> parameterType = parameter.getType();
        ToolParam paramAnno = parameter.getAnnotation(ToolParam.class);

        String type = parameterType.isArray() ? "array" : parameterType.getSimpleName().toLowerCase();
        Object[] enums = parameterType.isEnum() ? parameterType.getEnumConstants() : new Object[0];

        String description = paramAnno.description();
        boolean required = paramAnno.required();

        List<ToolResolveItem> properties = new ArrayList<>();
        // 如果是非基础对象，就要迭代
        if (!parameterType.isPrimitive()) {
            // 非基础对象，又是数组
            if (parameterType.isArray()) {
                Class<?> componentType = parameterType.getComponentType();
                // 数组封装的又是基础对象
                if (componentType.isPrimitive() || componentType.equals(String.class)) {
                    // 构造一个特殊item，在构造json时判断
                    properties.add(new ToolResolveItem("",
                            componentType.getSimpleName().toLowerCase(),
                            componentType.isEnum() ? componentType.getEnumConstants() : new Object[0],
                            "", true, new ArrayList<>()));
                } else {
                    // 非基础对象，又是数组，数组封装的又是封装对象
                    properties.addAll(getAnnotatedFields(componentType).stream().map(ToolResolveUtil::getToolResolveItem).toList());
                }
            } else {
                // 非基础对象，又不是数组，那就是单纯的封装对象
                properties.addAll(getAnnotatedFields(parameterType).stream().map(ToolResolveUtil::getToolResolveItem).toList());
            }
        }

        return new ToolResolveItem(name, type, enums, description, required, properties);
    }

    private static ToolResolveItem getToolResolveItem(Field parameter) {
        String name = parameter.getName();
        Class<?> parameterType = parameter.getType();
        ToolParam paramAnno = parameter.getAnnotation(ToolParam.class);

        String type = parameterType.isArray() ? "array" : parameterType.getSimpleName().toLowerCase();
        Object[] enums = parameterType.isEnum() ? parameterType.getEnumConstants() : new Object[0];

        String description = paramAnno.description();
        boolean required = paramAnno.required();

        List<ToolResolveItem> properties = new ArrayList<>();
        // 如果是非基础对象，就要迭代
        if (!parameterType.isPrimitive()) {
            // 非基础对象，又是数组
            if (parameterType.isArray()) {
                Class<?> componentType = parameterType.getComponentType();
                // 数组封装的又是基础对象
                if (componentType.isPrimitive() || componentType.equals(String.class)) {
                    // 构造一个特殊item，在构造json时判断
                    properties.add(new ToolResolveItem("",
                            componentType.getSimpleName().toLowerCase(),
                            componentType.isEnum() ? componentType.getEnumConstants() : new Object[0],
                            "", true, new ArrayList<>()));
                } else {
                    // 非基础对象，又是数组，数组封装的又是封装对象
                    properties.addAll(getAnnotatedFields(componentType).stream().map(ToolResolveUtil::getToolResolveItem).toList());
                }
            } else {
                // 非基础对象，又不是数组，那就是单纯的封装对象
                properties.addAll(getAnnotatedFields(parameterType).stream().map(ToolResolveUtil::getToolResolveItem).toList());
            }
        }

        return new ToolResolveItem(name, type, enums, description, required, properties);
    }

    private static List<Method> getAnnotatedMethods(Class<?> obj) {
        return Arrays.stream(obj.getMethods()).filter(ToolResolveUtil::checkMethod).toList();
    }

    private static boolean checkMethod(Method method) {
        return method.getAnnotation(ToolMethod.class) != null;
    }

    private static List<Field> getAnnotatedFields(Class<?> obj) {
        return Arrays.stream(obj.getDeclaredFields()).filter(ToolResolveUtil::checkFields).toList();
    }

    private static boolean checkFields(Field field) {
        return field.getAnnotation(ToolParam.class) != null;
    }
}
