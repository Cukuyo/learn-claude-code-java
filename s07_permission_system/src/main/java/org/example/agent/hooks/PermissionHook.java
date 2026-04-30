package org.example.agent.hooks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.agent.IAgent;
import org.example.permission.PermissonRule;
import org.example.permission.PermissonUtil;

import com.alibaba.fastjson2.JSONObject;

public class PermissionHook implements AgentHook{
    private Path denyPath;
    private Path allowPath;

    private Map<String,List<PermissonRule>> denyHooks;
    private Map<String,List<PermissonRule>> allowHooks;

    public PermissionHook(Path denyPath,Path allowPath) throws IOException{
        this.denyPath=denyPath;
        this.allowPath=allowPath;

         for (PermissonRule rule : PermissonUtil.read(denyPath)) {
            if (denyHooks.containsKey(rule.toolName())) {
                denyHooks.get(rule.toolName()).add(rule);
            }else{
                denyHooks.put(rule.toolName(), new ArrayList<>());
                denyHooks.get(rule.toolName()).add(rule);
            }
         }
         
         for (PermissonRule rule : PermissonUtil.read(allowPath)) {
            if (allowHooks.containsKey(rule.toolName())) {
                allowHooks.get(rule.toolName()).add(rule);
            }else{
                allowHooks.put(rule.toolName(), new ArrayList<>());
                allowHooks.get(rule.toolName()).add(rule);
            }
         }
    }

    @Override
    public String hookToolUse(IAgent agent, String id, String name, JSONObject arguments) {
        if (!name.equalsIgnoreCase("execute")) {
            return null;
        }
        switch (name) {
            case "execute":
                String command =arguments.getString("command");
                List<PermissonRule> list = denyHooks.get(command);
                for(PermissonRule rule:list){
                    rule.content().
                }
                break;
        
            default:
                break;
        }

        return null;
    }
}
