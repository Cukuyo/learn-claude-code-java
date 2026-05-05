package org.example.permission;

import java.util.regex.Pattern;

public record PermissionRule(
        String toolName, String content, Pattern pattern,
        String autoBehavior, String autoBehaviorReason,
        String masterBehavior, String masterBehaviorReason,
        String godBehavior, String godBehaviorReason) {

    @Override
    public String toString() {
        return toolName + "," + content + "," + autoBehavior + "," + autoBehaviorReason + "," + masterBehavior + "," + masterBehaviorReason + "," + godBehavior + "," + godBehaviorReason;
    }
}
