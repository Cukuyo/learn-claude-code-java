package org.example.permission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 权限文件工具类
 */
public class PermissionFileUtil {
    /**
     * 读配置
     *
     * @param path path
     * @return List<PermissionRule>
     * @throws IOException IOException
     */
    public static List<PermissionRule> read(Path path) throws IOException {
        List<PermissionRule> list = new ArrayList<>();
        for (String line : Files.readAllLines(path)) {
            String[] arr = line.split(",");
            list.add(new PermissionRule(
                    arr[0].trim(), arr[1].trim(), Pattern.compile(arr[1].trim()), arr[2].trim(),
                    arr[3].trim(), arr[4].trim(), arr[5].trim(), arr[6].trim(), arr[7].trim()));
        }

        return list;
    }

    /**
     * 写配置
     *
     * @param path path
     * @param list list
     * @throws IOException IOException
     */
    public static void write(Path path, List<PermissionRule> list) throws IOException {
        StringBuilder builder = new StringBuilder(list.size() * 128);
        for (PermissionRule rule : list) {
            builder.append(rule.toString()).append(System.lineSeparator());
        }
        Files.writeString(path, builder.toString());
    }
}
