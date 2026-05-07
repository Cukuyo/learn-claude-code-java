package org.example.define_systems.memory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * memory file工具类
 */
public class MemoryFileUtil {
    /**
     * 起始分隔符
     */
    private static final String SEPARATOR = "---";

    public static String write(Path filePath, MemoryEntity memory) throws IOException {
        // 1. 校验记忆类型
        // 2. 生成安全文件名（替换特殊字符，对齐Python的safe_name）
        String safeName = memory.name.toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_");
        Path memoryFile = filePath.resolve(safeName + ".md");
        // 3. 写入md文件（frontmatter + 内容）
        String frontmatter = String.format(
                """
                        ---%s
                        name: %s%s
                        description: %s%s
                        type: %s%s
                        ---%s
                        %s
                        """,
                System.lineSeparator(),
                memory.name, System.lineSeparator(),
                memory.description, System.lineSeparator(),
                memory.type, System.lineSeparator(),
                System.lineSeparator(),
                memory.content);
        Files.write(memoryFile, frontmatter.getBytes());
        // 6. 返回结果（对齐Python的返回值）
        return String.format("Saved memory '%s' [%s] to %s",
                memory.getName(), memory.getType(), memoryFile);
    }

    /**
     * 解析指定目录下所有的memories
     *
     * @param dirPath skill目录
     * @return 该路径下所有的skills信息
     */
    public static List<MemoryEntity> resolveDir(Path dirPath) {
        try (Stream<Path> paths = Files.walk(dirPath)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> !"MEMORY.md".equalsIgnoreCase(path.getFileName().toString()))
                    .map(path -> {
                        try {
                            return readFile(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析memory文件的信息
     *
     * @param path skill.md文件
     * @return 该路径下所有的skills信息
     * @throws IOException IOException
     */
    private static MemoryEntity readFile(Path path) throws IOException {
        Map<String, String> meta = new HashMap<>();
        int separatorNum = 0;
        for (String line : Files.readAllLines(path)) {
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
            String[] arr = line.split(":");
            meta.put(arr[0].trim(), arr[1].trim());
        }

        MemoryEntity memoryEntity = new MemoryEntity();
        memoryEntity.name = meta.get("name");
        memoryEntity.description = meta.get("description");
        memoryEntity.type = MemoryEntity.MemoryType.valueOf(meta.get("name"));
        memoryEntity.content = meta.get("name");
        memoryEntity.storagePath = path;
        memoryEntity.updateTime = path.toFile().;
        return new MemoryEntity();
    }
}
