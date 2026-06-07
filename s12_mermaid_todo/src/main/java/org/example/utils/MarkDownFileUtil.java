package org.example.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * md文件工具类
 */
public class MarkDownFileUtil {
    private static final String SEPARATOR = "---";
    private static final Pattern LINE_HEAD_FORMAT = Pattern.compile("\\S+:.*");

    /**
     * 读取 md 元数据
     *
     * @param filePath filePath
     * @return 元数据
     * @throws IOException IOException
     */
    public static Map<String, String> readMeta(Path filePath) throws IOException {
        Map<String, String> meta = new HashMap<>();
        int separatorNum = 0;
        String lastKey = null;
        for (String line : Files.readAllLines(filePath)) {
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith(SEPARATOR)) {
                separatorNum++;
                continue;
            }
            if (separatorNum == 2) {
                break;
            }
            if (LINE_HEAD_FORMAT.matcher(line).matches()) {
                String[] arr = line.split(":");
                if (arr.length == 1) {
                    meta.put(arr[0].trim(), "");
                } else {
                    meta.put(arr[0].trim(), arr[1].trim());
                }
                lastKey = arr[0].trim();
            } else {
                meta.put(lastKey, meta.get(lastKey) + line);
            }
        }

        return meta;
    }

    /**
     * 读取 md 内容
     *
     * @param filePath filePath
     * @return 内容
     * @throws IOException IOException
     */
    public static String readContent(Path filePath) throws IOException {
        return Files.readString(filePath).replaceFirst("(?s)" + SEPARATOR + ".*?" + SEPARATOR, "");
    }

    /**
     * 向指定路径写入 md 元数据
     *
     * @param filePath filePath
     * @param meta     元数据
     * @throws IOException IOException
     */
    public static void writeMeta(Path filePath, Map<String, String> meta) throws IOException {
        if (!Files.exists(filePath)) {
            filePath.getParent().toFile().mkdirs();
        }

        StringBuilder builder = new StringBuilder(meta.size() * 128);
        builder.append(SEPARATOR).append(System.lineSeparator());
        for (Map.Entry<String, String> entry : meta.entrySet()) {
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append(System.lineSeparator());
        }
        builder.append(SEPARATOR).append(System.lineSeparator());

        Files.writeString(filePath, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    /**
     * 向指定路径写入 md 内容
     *
     * @param filePath filePath
     * @param content  内容
     * @throws IOException IOException
     */
    public static void writeContent(Path filePath, String content) throws IOException {
        Map<String, String> meta = readMeta(filePath);
        writeMeta(filePath, meta);

        Files.writeString(filePath, content, StandardOpenOption.APPEND);
    }
}
