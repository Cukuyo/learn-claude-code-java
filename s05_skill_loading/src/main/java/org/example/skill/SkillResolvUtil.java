package org.example.skill;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * skill 解析工具类
 */
public class SkillResolvUtil {
    /**
     * 起始分隔符
     */
    private static final String SEPARATOR = "---";

    /**
     * 解析skill目录下所有的skills
     *
     * @param dirPath skill目录
     * @return 该路径下所有的skills信息
     */
    public static List<SkillManifest> resolveDir(Path dirPath) {
        try (Stream<Path> paths = Files.walk(dirPath)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> "SKILL.md".equalsIgnoreCase(path.getFileName().toString()))
                    .map(path -> {
                        try {
                            return resolveFile(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 接下skill文件的信息
     *
     * @param path skill.md文件
     * @return 该路径下所有的skills信息
     * @throws IOException IOException
     */
    private static SkillManifest resolveFile(Path path) throws IOException {
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

        return new SkillManifest(meta.get("name"), meta.get("description"), path.toFile().getParentFile().toPath());
    }
}
