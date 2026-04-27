package org.example.utils;

import org.example.tool.ToolMethod;
import org.example.tool.ToolParam;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.*;

/**
 * 操作系统命令行工具类
 */
public class CommandUtil {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    /**
     * 默认超时时间，10S
     */
    private static final long DEFAULT_TIMEOUT = 10;

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
    @ToolMethod(description = "在当前工作目录下执行shell命令，windows下为cmc /c， linux下为bash -c")
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
        CommandAsyncReader commandAsyncReader = null;
        Future<String> commandFuture;

        try {
            process = pb.start();

            // 异步接收返回
            commandAsyncReader = new CommandAsyncReader(process.getInputStream(), OS_CHARSET);
            commandFuture = EXECUTOR.submit(commandAsyncReader);

            // 尝试等待执行完成
            if (!process.waitFor(timeout, timeUnit)) {
                destroyProcess(process, commandAsyncReader);
                return String.format(RESULT_FORMAT, "超时", "\\", commandFuture.get());
            } else {
                int exitCode = process.exitValue();
                return String.format(RESULT_FORMAT, exitCode == 0 ? "成功" : "失败", exitCode, commandFuture.get());
            }
        } catch (UnsupportedEncodingException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return String.format(RESULT_FORMAT, "IO异常", "\\", "");
        } finally {
            destroyProcess(process, commandAsyncReader);
        }
    }

    /**
     * 安全销毁进程
     */
    private static void destroyProcess(Process process, CommandAsyncReader commandAsyncReader) {
        if (commandAsyncReader != null) {
            commandAsyncReader.stop();
        }
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
