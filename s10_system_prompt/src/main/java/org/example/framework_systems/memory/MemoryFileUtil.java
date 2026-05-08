package org.example.framework_systems.memory;

import org.example.utils.DateUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * memory file工具类
 */
public class MemoryFileUtil {
    /**
     * 起始分隔符
     */
    private static final String SEPARATOR = "---";

    /**
     * 写入信息到memory文件
     *
     * @param path   memory 文件夹
     * @param memory memory
     * @return 写入结果
     * @throws IOException
     */
    public static String write(Path dirPath, MemoryEntity memory) throws IOException {
        String frontmatter = String.format(
                """
                %s%s
                name: %s%s
                description: %s%s
                type: %s%s
                %s%s
                %s
                """,
                SEPARATOR, System.lineSeparator(),
                memory.name, System.lineSeparator(),
                memory.description, System.lineSeparator(),
                memory.type, System.lineSeparator(),
                SEPARATOR, System.lineSeparator(),
                memory.content);
        Path memoryFile = dirPath.resolve(memory.type + "_" + memory.name + ".md");
        Files.write(dirPath.resolve(memory.name + ".md"), frontmatter.getBytes());

        memory.storagePath = memoryFile;
        memory.updateTime = DateUtil.transLong2LocalDateTime(System.currentTimeMillis());

        return String.format("<%s>已成功写入%s", memory.name, memoryFile);

    }

    /**
     * 解析memory文件的信息
     *
     * @param path memory.md文件
     * @return memory信息
     * @throws IOException IOException
     */
    public static MemoryEntity readFile(Path path) throws IOException {
        Map<String, String> meta = new HashMap<>();
        StringBuilder builder = new StringBuilder(128);
        int separatorNum = 0;
        for (String line : Files.readAllLines(path)) {
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith(SEPARATOR)) {
                separatorNum++;
                continue;
            }
            if (separatorNum == 1) {
                String[] arr = line.split(":");
                meta.put(arr[0].trim(), arr[1].trim());
            } else {
                builder.append(line).append(System.lineSeparator());
            }
        }

        MemoryEntity memoryEntity = new MemoryEntity();
        memoryEntity.name = meta.get("name");
        memoryEntity.description = meta.get("description");
        memoryEntity.type = MemoryType.valueOf(meta.get("name"));
        memoryEntity.content = builder.toString();
        memoryEntity.storagePath = path;
        memoryEntity.updateTime = DateUtil.transLong2LocalDateTime(path.toFile().lastModified());
        return memoryEntity;
    }
}
