package org.example.framework_systems.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * task 文件夹工具类
 */
public class TaskDirUtil {
    /**
     * 解析task目录下所有的tasks
     *
     * @param dirPath task目录
     * @return 该路径下所有的tasks信息
     */
    public static List<TaskEntity> resolveDir(Path dirPath) {
        try (Stream<Path> paths = Files.walk(dirPath)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".md"))
                    .map(path -> {
                        try {
                            return TaskFileUtil.readFile(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
