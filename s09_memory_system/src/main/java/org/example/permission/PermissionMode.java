package org.example.permission;

/**
 * 权限模式
 */
public enum PermissionMode {
    /**
     * 全自动，最低权限 → 高危命令全 deny
     */
    AUTO,
    /**
     * 主人权限 → 危险操作全 ask
     */
    MASTER,
    /**
     * 神权限 → 除极端风险外全 allow
     */
    GOD;
}
