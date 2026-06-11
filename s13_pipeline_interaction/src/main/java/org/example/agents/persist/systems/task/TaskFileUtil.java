package org.example.agents.persist.systems.task;

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
     * 写入信息到task文件
     *
     * @param dirPath task 文件夹
     * @param task    task
     * @throws IOException IOException
     */
    public static void write(Path dirPath, TaskEntity task) throws IOException {
        Path filePath = dirPath.resolve(task.buildFileName());
        MarkDownFileUtil.writeMeta(filePath, task.toMeta());
        MarkDownFileUtil.writeContent(filePath, task.content);

        task.filePath = filePath;
        task.updateTime = DateUtil.transLong2LocalDateTime(System.currentTimeMillis());
    }

    /**
     * 解析task文件的信息
     *
     * @param filePath task.md文件
     * @return task
     * @throws IOException IOException
     */
    public static TaskEntity readFile(Path filePath) throws IOException {
        Map<String, String> meta = MarkDownFileUtil.readMeta(filePath);

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.agentName = meta.get("agentName");
        taskEntity.name = meta.get("name");
        taskEntity.description = meta.get("description");
        taskEntity.content = MarkDownFileUtil.readContent(filePath);
        taskEntity.filePath = filePath;
        taskEntity.updateTime = DateUtil.transLong2LocalDateTime(filePath.toFile().lastModified());
        return taskEntity;
    }
}
