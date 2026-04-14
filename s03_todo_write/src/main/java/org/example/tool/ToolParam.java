package org.example.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * tool函数参数声明
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolParam {
    /**
     * @return tool函数参数描述
     */
    String description();

    /**
     * @return 是否必选
     */
    boolean required() default true;
}
