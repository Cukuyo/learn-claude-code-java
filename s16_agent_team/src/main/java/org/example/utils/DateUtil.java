package org.example.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/*
* 日期工具类
*/
public class DateUtil {
    
    /*
    * long 转换为 LocalDateTime
    */
    public static LocalDateTime transLong2LocalDateTime(long time){
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(time),
            ZoneId.systemDefault()
        );
    }
}
