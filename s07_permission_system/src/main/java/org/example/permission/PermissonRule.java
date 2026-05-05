package org.example.permission;

import java.util.regex.Pattern;

public record PermissonRule(
    String toolName,String content,Pattern pattern,
    String autoBehavior,String autoBehaviorReson,
    String masterBehavior,String masterBehaviorReson,
    String godBehavior,String godBehaviorReson) {
    
    @Override
    public final String toString() {
        return toolName+","+content+","+autoBehavior+","+autoBehaviorReson+","+masterBehavior+","+masterBehaviorReson+","+godBehavior+","+godBehaviorReson;
    }
}
