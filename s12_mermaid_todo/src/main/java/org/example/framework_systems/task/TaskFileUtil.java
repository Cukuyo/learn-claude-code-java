package org.example.framework_systems.task;

import org.example.utils.DateUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * task file工具类
 */
public class TaskFileUtil {
    /**
     * 起始分隔符
     */
    private static final String SEPARATOR = "---";

    /**
     * 写入信息到task文件
     *
     * @param dirPath task 文件夹
     * @param task    task
     * @return 写入结果
     * @throws IOException IOException
     */
    public static String write(Path dirPath, TaskEntity task) throws IOException {
        String frontmatter = String.format(
                """
                        %s
                        agent: %s
                        name: %s
                        description: %s
                        process: %d
                        %s
                        %s
                        """,
                SEPARATOR,
                task.agent,
                task.name,
                task.description,
                task.progress,
                SEPARATOR,
                task.content);
        Path memoryFile = dirPath.resolve(task.agent + "_" + task.name + ".md");
        Files.write(memoryFile, frontmatter.getBytes());

        task.storagePath = memoryFile;
        task.updateTime = DateUtil.transLong2LocalDateTime(System.currentTimeMillis());

        return String.format("<%s>已成功写入%s", task.name, memoryFile);

    }

    /**
     * 解析task文件的信息
     *
     * @param path task.md文件
     * @return task
     * @throws IOException IOException
     */
    public static TaskEntity readFile(Path path) throws IOException {
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

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.agent = meta.get("agent");
        taskEntity.name = meta.get("name");
        taskEntity.description = meta.get("description");
        taskEntity.content = builder.toString();
        taskEntity.storagePath = path;
        taskEntity.updateTime = DateUtil.transLong2LocalDateTime(path.toFile().lastModified());
        return taskEntity;
    }
}
