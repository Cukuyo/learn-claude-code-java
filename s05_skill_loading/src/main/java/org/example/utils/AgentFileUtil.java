package org.example.utils;

import org.example.tool.ToolMethod;
import org.example.tool.ToolParam;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 对应Python工具类的文件操作封装
 * 实现safe_path、read、write、edit核心逻辑
 */
public class AgentFileUtil {
    /**
     * 工作目录（对应Python中的WORKDIR）
     */
    private static final Path WORKDIR = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

    /**
     * 安全路径校验（对应Python的safe_path函数）
     * 防止路径逃逸出工作目录
     */
    private static Path safePath(String pathStr) throws IllegalArgumentException {
        Path resolvedPath = WORKDIR.resolve(Paths.get(pathStr)).toAbsolutePath().normalize();
        // 校验路径是否在工作目录下
        if (!resolvedPath.startsWith(WORKDIR)) {
            throw new IllegalArgumentException("Path escapes workspace: " + pathStr);
        }
        return resolvedPath;
    }

    /**
     * 重载readFile
     *
     * @param pathStr 文件路径
     * @return 文件内容
     */
    @ToolMethod(description = "读取文件")
    public static String readFile(@ToolParam(description = "文件路径，支持绝对路径和相对路径") String pathStr) {
        try {
            return Files.readString(safePath(pathStr));
        } catch (Exception e) {
            return "Error: " + e;
        }
    }

    /**
     * 写入文件内容（对应Python的run_write函数）
     *
     * @param pathStr 文件路径
     * @param content 文件内容
     * @return 写入结果
     */
    @ToolMethod(description = "写入文件，会自动创建父目录，若存在原文件则覆盖")
    public static String writeFile(@ToolParam(description = "文件路径，支持绝对路径和相对路径") String pathStr,
                                   @ToolParam(description = "待写入的文本") String content) {
        try {
            Path path = safePath(pathStr);
            // 创建父目录（如果不存在）
            Files.createDirectories(path.getParent());
            // 写入内容（覆盖原有内容）
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "写入成功： " + pathStr;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 编辑文件（替换指定文本，对应Python的run_edit函数）
     *
     * @param pathStr 文件路径
     * @param oldText 旧文本
     * @param newText 新文本
     * @return 编辑结果
     */
    @ToolMethod(description = "编辑文件，将第一个匹配的旧文本替换为新文本")
    public static String editFile(@ToolParam(description = "文件路径，支持绝对路径和相对路径") String pathStr,
                                  @ToolParam(description = "待替换的旧文本") String oldText,
                                  @ToolParam(description = "待替换的新文本") String newText) {
        try {
            Path path = safePath(pathStr);
            String content = Files.readString(path);

            // 检查旧文本是否存在
            if (!content.contains(oldText)) {
                return "Error: 该文件不存在 " + pathStr;
            }

            // 替换第一个匹配的旧文本
            String newContent = content.replaceFirst(oldText, newText);
            Files.writeString(path, newContent);
            return "编辑成功： " + pathStr;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // 测试示例
    public static void main(String[] args) {
        // 测试写入
        System.out.println(writeFile("test.txt", "Hello World!"));
        // 测试读取
        System.out.println(readFile("test.txt"));
        // 测试编辑
        System.out.println(editFile("test.txt", "World", "Java"));
    }
}
