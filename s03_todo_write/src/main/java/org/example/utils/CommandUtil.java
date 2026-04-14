package org.example.utils;

import org.example.tool.ToolMethod;
import org.example.tool.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

/**
 * 操作系统命令行工具类
 */
public class CommandUtil {
    /**
     * 默认超时时间，3S
     */
    private static final long DEFAULT_TIMEOUT = 3;

    /**
     * 响应格式，可采用record进行更标准的替代
     */
    private static final String RESULT_FORMAT = "命令执行结果：%s, 退出码：%s, 输出：" + System.lineSeparator() + "%s ";

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final String[] RUN_ENGINE = IS_WINDOWS ? new String[]{"cmd", "/c"} : new String[]{"bash", "-c"};
    private static final String OS_CHARSET = IS_WINDOWS ? "GBK" : "UTF-8";

    /**
     * 执行系统命令（自动识别平台）
     *
     * @param command 命令
     * @return 执行结果
     */
    @ToolMethod(description = "Run a shell command in the current workspace.")
    public static String execute(@ToolParam(description = "shell command") String command) {
        return execute(command, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * 执行系统命令（带超时）
     *
     * @param command  命令
     * @param timeout  超时时间
     * @param timeUnit 时间单位
     * @return 执行结果
     */
    public static String execute(String command, long timeout, TimeUnit timeUnit) {
        if (!CommandSecurityChecker.isSafeCommand(command)) {
            return "禁止执行高危命令：" + command + "！";
        }

        ProcessBuilder pb = new ProcessBuilder(RUN_ENGINE[0], RUN_ENGINE[1], command);
        pb.redirectErrorStream(true);

        Process process = null;
        StringBuilder result = new StringBuilder();

        try {
            process = pb.start();

            // 尝试等待执行完成
            if (!process.waitFor(timeout, timeUnit)) {
                destroyProcess(process);
                return String.format(RESULT_FORMAT, "超时", "\\", result.toString().trim());
            }

            // 尝试读取已有输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), OS_CHARSET))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append(System.lineSeparator());
                }
            }

            int exitCode = process.exitValue();
            return String.format(RESULT_FORMAT, exitCode == 0 ? "成功" : "失败", exitCode, result.toString().trim());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return String.format(RESULT_FORMAT, "IO异常", "\\", result.toString().trim());
        } catch (InterruptedException e) {
            return String.format(RESULT_FORMAT, "线程中断", "\\", result.toString().trim());
        } finally {
            destroyProcess(process);
        }
    }

    /**
     * 安全销毁进程
     */
    private static void destroyProcess(Process process) {
        if (process != null) {
            process.destroy();
        }
    }

    // ==================== 测试 ====================
    public static void main(String[] args) {
        String res = CommandUtil.execute("dir");
        System.out.println("Windows 目录：\n" + res);
        res = CommandUtil.execute("echo Hello Windows from Java");
        System.out.println("\n输出：" + res);
    }
}
