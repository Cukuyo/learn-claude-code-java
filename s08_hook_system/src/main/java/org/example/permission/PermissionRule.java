package org.example.permission;

import java.util.regex.Pattern;

/**
 * 定义在文件里的权限控制格式
 */
public class PermissionRule {
    public String toolName;
    public String content;
    public Pattern pattern;
    public String autoBehavior;
    public String autoBehaviorReason;
    public String masterBehavior;
    public String masterBehaviorReason;
    public String godBehavior;
    public String godBehaviorReason;

    public PermissionRule(String toolName, String content, Pattern pattern, String autoBehavior, String autoBehaviorReason,
                          String masterBehavior, String masterBehaviorReason, String godBehavior, String godBehaviorReason) {
        this.toolName = toolName;
        this.content = content;
        this.pattern = pattern;
        this.autoBehavior = autoBehavior;
        this.autoBehaviorReason = autoBehaviorReason;
        this.masterBehavior = masterBehavior;
        this.masterBehaviorReason = masterBehaviorReason;
        this.godBehavior = godBehavior;
        this.godBehaviorReason = godBehaviorReason;
    }

    public void update(PermissionMode mode, PermissionBehavior behavior, String behaviorReason) {
        switch (mode) {
            case AUTO -> {
                autoBehavior = behavior.name();
                autoBehaviorReason = behaviorReason;
            }
            case MASTER -> {
                masterBehavior = behavior.name();
                masterBehaviorReason = behaviorReason;
            }
            case GOD -> {
                godBehavior = behavior.name();
                godBehaviorReason = behaviorReason;
            }
            default -> {
            }
        }
    }

    public PermissionBehavior getBehavior(PermissionMode mode) {
        switch (mode) {
            case AUTO -> {
                return PermissionBehavior.valueOf(autoBehavior);
            }
            case MASTER -> {
                return PermissionBehavior.valueOf(masterBehavior);
            }
            case GOD -> {
                return PermissionBehavior.valueOf(godBehavior);
            }
            default -> {
                return null;
            }
        }
    }

    public String getBehaviorReason(PermissionMode mode) {
        switch (mode) {
            case AUTO -> {
                return autoBehaviorReason;
            }
            case MASTER -> {
                return masterBehaviorReason;
            }
            case GOD -> {
                return godBehaviorReason;
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return toolName + "," + content + "," + autoBehavior + "," + autoBehaviorReason + "," + masterBehavior + "," + masterBehaviorReason + "," + godBehavior + "," + godBehaviorReason;
    }
}
