package org.example.framework_skill;

import org.example.utils.MarkDownFileUtil;

import java.io.IOException;
import java.nio.file.Path;

/**
 * skill 文件工具类
 */
public class SkillFileUtil {
    /**
     * 读取指定路径下skill.md的body内容
     *
     * @param dirPath skill目录
     * @return skill.md body
     * @throws IOException IOException
     */
    public static String readSkillMDBody(Path dirPath) throws IOException {
        return MarkDownFileUtil.readContent(dirPath.resolve("SKILL.md"));
    }
}
