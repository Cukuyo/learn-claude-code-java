package org.example.framework_systems.task;

import org.example.utils.DateUtil;
import org.example.utils.MarkDownFileUtil;

import java.io.IOException;
import java.nio.file.Path;
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
        Path filePath = dirPath.resolve(task.agent + "_" + task.name + ".md");
        MarkDownFileUtil.writeMeta(filePath, task.toMeta());

        task.storagePath = filePath;
        task.updateTime = DateUtil.transLong2LocalDateTime(System.currentTimeMillis());

        return String.format("<%s>已成功写入%s", task.name, filePath);
    }

    /**
     * 解析task文件的信息
     *
     * @param path task.md文件
     * @return task
     * @throws IOException IOException
     */
    public static TaskEntity readFile(Path path) throws IOException {
        Map<String, String> meta = MarkDownFileUtil.readMeta(path);

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.agent = meta.get("agent");
        taskEntity.name = meta.get("name");
        taskEntity.description = meta.get("description");
        taskEntity.content = MarkDownFileUtil.readContent(path);
        taskEntity.storagePath = path;
        taskEntity.updateTime = DateUtil.transLong2LocalDateTime(path.toFile().lastModified());
        return taskEntity;
    }
}
