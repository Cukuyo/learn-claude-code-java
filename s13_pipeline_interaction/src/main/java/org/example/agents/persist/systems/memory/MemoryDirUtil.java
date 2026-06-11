package org.example.agents.persist.systems.memory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
            List<MemoryEntity> list = paths.filter(Files::isRegularFile)
                                           .filter(path -> path.toString().endsWith(".md"))
                                           .map(path -> {
                                               try {
                                                   return MemoryFileUtil.read(path);
                                               } catch (IOException e) {
                                                   throw new RuntimeException(e);
                                               }
                                           })
                                           .toList();
            return new ArrayList<>(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
