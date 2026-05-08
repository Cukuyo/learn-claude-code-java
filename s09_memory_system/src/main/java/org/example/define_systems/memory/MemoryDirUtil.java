package org.example.define_systems.memory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * memory 文件夹工具类
 */
public class MemoryDirUtil {
     /**
     * 解析memory目录下所有的memories
     *
     * @param dirPath memory目录
     * @return 该路径下所有的memories信息
     */
    public static List<MemoryEntity> resolveDir(Path dirPath) {
        try (Stream<Path> paths = Files.walk(dirPath)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.endsWith(".md"))
                    .map(path -> {
                        try {
                            return MemoryFileUtil.readFile(path);
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
