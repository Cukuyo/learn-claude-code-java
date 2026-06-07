package org.example.framework_skill;

import org.example.utils.MarkDownFileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * skill 文件夹工具类
 */
public class SkillDirUtil {
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
                            Map<String, String> meta = MarkDownFileUtil.readMeta(path);
                            return new SkillManifest(meta.get("name"), meta.get("description"), path.toFile().getParentFile().toPath());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
