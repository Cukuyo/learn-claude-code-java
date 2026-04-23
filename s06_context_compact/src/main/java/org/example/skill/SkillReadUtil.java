package org.example.skill;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * skill读取类
 */
public class SkillReadUtil {
    /**
     * 读取指定路径下skill.md的body内容
     *
     * @param dirPath skill目录
     * @return skill.md body
     * @throws IOException IOException
     */
    public static String readSkillMDBody(Path dirPath) throws IOException {
        return readSkillFileBody(dirPath, "SKILL.md").replaceFirst("(?s)---.*?---", "");
    }

    /**
     * 读取skill目录下指定文件名的内容
     *
     * @param dirPath skill目录
     * @return 指定文件名的内容
     * @throws IOException IOException
     */
    private static String readSkillFileBody(Path dirPath, String fileName) throws IOException {
        try (Stream<Path> paths = Files.walk(dirPath)) {
            Optional<Path> skillMDOptional = paths.filter(Files::isRegularFile)
                    .filter(path -> fileName.equalsIgnoreCase(path.getFileName().toString())).findFirst();
            if (skillMDOptional.isPresent()) {
                return Files.readString(skillMDOptional.get());
            } else {
                return String.format("<%s>目录下无<%s>文件，请确认路径后重试", dirPath, fileName);
            }
        }
    }
}
