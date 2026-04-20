package org.example;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.models.DeepseekModel;
import org.example.todo.TodoManager;
import org.example.tool.ToolExecuter;
import org.example.tool.ToolResolve;
import org.example.utils.AgentFileUtils;
import org.example.utils.CommandUtil;
import org.example.utils.ToolToModelTransformUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class S03_subagent {
    private static final DeepseekModel MODEL = new DeepseekModel(System.getenv("api_key"));
    private static final Map<String, ToolExecuter> TOOL_HANDLERS = new HashMap<>();
    private static final TodoManager TODO_MANAGER = new TodoManager();

    static void main() throws IOException, InterruptedException {
        MODEL.addSystemMessages("你是一个纯情的小猫娘，会帮助主人解决各种技术问题");
        registryTool(CommandUtil.class);
        registryTool(AgentFileUtils.class);
        registryTool(TODO_MANAGER);

        Scanner scanner = new Scanner(System.in);
        System.out.print("#>>>");
        while (scanner.hasNextLine()) {
            String cmd = scanner.nextLine();
            if (cmd.equals("q")) {
                break;
            }

            MODEL.addUserMessage(cmd);
            agentLoop();
            System.out.print("#>>>");
        }
    }

    private static void agentLoop() throws IOException, InterruptedException {
        while (true) {
            // 每次都需要记录LLM的响应
            JSONObject chatRsp = MODEL.chat();
            JSONObject message = chatRsp.getJSONObject("message");
            MODEL.addAssistantMessages(message);
            System.out.printf("你的纯情猫娘>>>%s%s", message.getString("content"), System.lineSeparator());

            // 非工具调用即刻返回
            if (!chatRsp.getString("finish_reason").equals("tool_calls")) {
                return;
            }

            // 记录todo的使用
            boolean useTodo = false;

            // 依次调用tools
            JSONArray toolCalls = message.getJSONArray("tool_calls");
            for (Object obj : toolCalls) {
                JSONObject toolCall = (JSONObject) obj;
                JSONObject function = toolCall.getJSONObject("function");

                // tool参数
                String id = toolCall.getString("id");
                String name = function.getString("name");
                JSONObject arguments = JSONObject.parse(function.getString("arguments"));

                System.out.printf("开始执行tool, id:%s, func:%s, args:%s %s", id, name, arguments, System.lineSeparator());
                String toolRsp = TOOL_HANDLERS.get(name).execute(arguments);
                System.out.printf("结束执行tool, id:%s, func:%s, args:%s , result:%s %s", id, name, arguments, toolRsp, System.lineSeparator());
                MODEL.addToolMessages(toolRsp, id);

                if (name.equals("updateTasks")) {
                    useTodo = true;
                }
            }

            // 更新任务项
            updateTasks(useTodo);
        }
    }

    private static void updateTasks(boolean useTodo) {
        if (useTodo) {
            TODO_MANAGER.noteRoundReset();
        } else {
            TODO_MANAGER.noteRoundWithoutUpdate();
            String reminder = TODO_MANAGER.reminder();
            if (!reminder.isEmpty()) {
                MODEL.addUserMessage(reminder);
            }
        }
    }

    private static void registryTool(Object toolObj) {
        registryTool(ToolResolve.resolve(toolObj));
    }

    private static void registryTool(Class<?> toolObj) {
        registryTool(ToolResolve.resolve(toolObj));
    }

    private static void registryTool(List<ToolResolve.ToolResolveResult> toolResolveResults) {
        for (ToolResolve.ToolResolveResult toolResolveResult : toolResolveResults) {
            TOOL_HANDLERS.put(toolResolveResult.name(), toolResolveResult.toolHandler());
            MODEL.addTool(ToolToModelTransformUtil.transform(toolResolveResult, MODEL));
        }
    }
}
