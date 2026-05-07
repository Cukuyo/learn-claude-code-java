package org.example.define_systems.memory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 基于文件系统的记忆管理器：对齐Python的MemoryManager
 */
public class FileSystemMemoryManager implements IMemoryManager {
    /**
     * 记忆根目录（对应Python的MEMORY_DIR）
     */
    private static final Path MEMORY_ROOT = Paths.get(System.getProperty("user.dir"), ".memory");
    /**
     * 记忆索引文件（对应Python的MEMORY_INDEX）
     */
    private static final Path MEMORY_INDEX = MEMORY_ROOT.resolve("MEMORY.md");
    /**
     * 内存缓存：name -> MemoryEntity
     */
    private final Map<String, MemoryEntity> memoryCache = new HashMap<>();

    @Override
    public void loadAllMemories() throws IOException {
        // 1. 创建.memory目录（不存在则创建）
        if (!Files.exists(MEMORY_ROOT)) {
            MEMORY_ROOT.toFile().mkdirs();
        }

        try (Stream<Path> paths = Files.walk(MEMORY_ROOT)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> !"MEMORY.md".equalsIgnoreCase(path.getFileName().toString()))
                    .map(path -> {
                        try {
                            return resolveFile(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        // 2. 扫描所有.md文件（排除MEMORY.md）
        // 3. 解析每个md文件的frontmatter（---包裹的元数据），构建MemoryEntity
        // 4. 加载到memoryCache
        // （实现参考Python的_parse_frontmatter，可用正则/MD解析库如commonmark）
    }

    @Override
    public String saveMemory(MemoryEntity memory) throws IOException {
        // 1. 校验记忆类型
        // 2. 生成安全文件名（替换特殊字符，对齐Python的safe_name）
        String safeName = memory.getName().toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_");
        Path memoryFile = MEMORY_ROOT.resolve(safeName + ".md");
        // 3. 写入md文件（frontmatter + 内容）
        String frontmatter = String.format("---\nname: %s\ndescription: %s\ntype: %s\n---\n%s",
                memory.getName(), memory.getDescription(), memory.getType(), memory.getContent());
        Files.write(memoryFile, frontmatter.getBytes());
        // 4. 更新内存缓存
        memoryCache.put(memory.getName(), memory);
        // 5. 重建MEMORY.md索引
        rebuildIndex();
        // 6. 返回结果（对齐Python的返回值）
        return String.format("Saved memory '%s' [%s] to %s",
                memory.getName(), memory.getType(), memoryFile);
    }

    @Override
    public String buildMemoryPrompt() {
        // 1. 按类型分组拼接记忆文本（对齐Python的load_memory_prompt）
        // 2. 格式：# Memories (persistent across sessions) + 按类型分块
        StringBuilder prompt = new StringBuilder();
        prompt.append("# Memories (persistent across sessions)\n\n");
        for (MemoryEntity.MemoryType type : MemoryEntity.MemoryType.values()) {
            // 过滤该类型的记忆
            memoryCache.values().stream()
                    .filter(m -> m.getType() == type)
                    .forEach(m -> {
                        prompt.append("## [").append(type.name()).append("]\n");
                        prompt.append("### ").append(m.getName()).append(": ").append(m.getDescription()).append("\n");
                        prompt.append(m.getContent()).append("\n\n");
                    });
        }
        return prompt.toString();
    }

    @Override
    public MemoryEntity getMemory(String name) {
        return memoryCache.get(name);
    }

    @Override
    public void pruneExpiredMemories() {

    }

    // 重建MEMORY.md索引（对齐Python的_rebuild_index）
    private void rebuildIndex() {
        StringBuilder index = new StringBuilder("# Memory Index\n\n");
        memoryCache.values().forEach(m ->
                index.append("- ").append(m.getName()).append(": ").append(m.getDescription())
                        .append(" [").append(m.getType()).append("]\n")
        );
        Files.write(MEMORY_INDEX, index.toString().getBytes());
    }

    // 其他方法实现...
}
