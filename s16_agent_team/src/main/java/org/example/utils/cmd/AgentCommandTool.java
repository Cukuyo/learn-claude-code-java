package org.example.utils.cmd;

import org.example.agent.tool.ToolExecuter;
import org.example.agent.tool.ToolMethod;
import org.example.agent.tool.ToolParam;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * 操作系统命令行工具类
 */
public class AgentCommandTool {
    private static final ThreadFactory THREAD_FACTORY = Thread.ofVirtual().name("AgentCommandTool", 0).factory();
    private static final ExecutorService EXECUTOR = Executors.newThreadPerTaskExecutor(THREAD_FACTORY);

    /**
     * 响应格式，可采用record进行更标准的替代
     */
    private static final String RESULT_FORMAT = "命令执行结果：%s, 退出码：%s, 输出：" + System.lineSeparator() + "%s ";

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final String[] RUN_ENGINE = IS_WINDOWS ? new String[]{"cmd", "/c"} : new String[]{"bash", "-c"};

    /**
     * 执行系统命令（自动识别平台）
     *
     * @param command 命令
     * @return 执行结果
     */
    @ToolMethod(description = "在当前工作目录下执行shell命令，windows下为cmc /c command， linux下为bash -c command")
    public static Future<String> execute(
            @ToolParam(description = "command") String command,
            @ToolParam(description = "执行超时时间，超时后再次执行要注意幂等性检查") int timeOut,
            @ToolParam(description = "是否要异步执行，对于耗时较长的命令可以多个命令异步执行以提高效率") boolean isAsync) {
        if (isAsync) {
            return EXECUTOR.submit(() -> execute(command, timeOut, TimeUnit.SECONDS));
        } else {
            return ToolExecuter.simpleRsp(execute(command, timeOut, TimeUnit.SECONDS));
        }
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
        ProcessBuilder pb = new ProcessBuilder(RUN_ENGINE[0], RUN_ENGINE[1], command);
        pb.redirectErrorStream(true);

        Process process = null;
        CommandAsyncReader commandAsyncReader = null;
        Future<String> commandFuture = null;

        try {
            process = pb.start();

            // 异步接收返回
            commandAsyncReader = new CommandAsyncReader(process.getInputStream());
            commandFuture = EXECUTOR.submit(commandAsyncReader);

            // 尝试等待执行完成
            if (!process.waitFor(timeout, timeUnit)) {
                destroyProcess(process, commandAsyncReader);
                return String.format(RESULT_FORMAT, "超时", "\\", commandFuture.get());
            } else {
                int exitCode = process.exitValue();
                return String.format(RESULT_FORMAT, exitCode == 0 ? "成功" : "失败", exitCode, commandFuture.get());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return String.format(RESULT_FORMAT, "无法执行", "\\", e.getMessage());
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
}
