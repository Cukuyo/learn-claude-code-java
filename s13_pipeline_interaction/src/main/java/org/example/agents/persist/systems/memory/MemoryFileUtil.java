package org.example.agents.persist.systems.memory;

import org.example.utils.DateUtil;
import org.example.utils.MarkDownFileUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * memory file工具类
 */
public class MemoryFileUtil {
    /**
     * 写入信息到memory文件
     *
     * @param dirPath memory 文件夹
     * @param memory  memory
     * @throws IOException IOException
     */
    public static void write(Path dirPath, MemoryEntity memory) throws IOException {
        Path filePath = dirPath.resolve(memory.buildFileName());
        MarkDownFileUtil.writeMeta(filePath, memory.toMeta());
        MarkDownFileUtil.writeContent(filePath, memory.content);

        memory.filePath = filePath;
        memory.updateTime = LocalDateTime.now();
    }

    /**
     * 解析memory文件的信息
     *
     * @param filePath memory.md文件
     * @return memory信息
     * @throws IOException IOException
     */
    public static MemoryEntity read(Path filePath) throws IOException {
        Map<String, String> meta = MarkDownFileUtil.readMeta(filePath);

        MemoryEntity memoryEntity = new MemoryEntity();
        memoryEntity.name = meta.get("name");
        memoryEntity.description = meta.get("description");
        memoryEntity.type = MemoryType.valueOf(meta.get("type"));
        memoryEntity.content = MarkDownFileUtil.readContent(filePath);
        memoryEntity.filePath = filePath;
        memoryEntity.updateTime = DateUtil.transLong2LocalDateTime(filePath.toFile().lastModified());
        return memoryEntity;
    }
}
