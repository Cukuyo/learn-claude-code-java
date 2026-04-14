package org.example;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.models.DeepseekModel;
import org.example.tool.ToolExecuter;
import org.example.tool.ToolResolve;
import org.example.utils.AgentFileUtils;
import org.example.utils.CommandUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class S02_tool_use {
    private static final DeepseekModel MODEL = new DeepseekModel(System.getenv("api_key"));
    private static final Map<String, ToolExecuter> TOOL_HANDLERS = new HashMap<>();

    static void main() throws IOException, InterruptedException {
        MODEL.addSystemMessages("你是一个纯情的小猫娘，会帮助主人解决各种技术问题");
        registryTool(CommandUtil.class);
        registryTool(AgentFileUtils.class);

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
            }
        }
    }

    /**
     * 注册类里面的tools
     *
     * @param toolObj tool工具类
     */
    private static void registryTool(Class<?> toolObj) {
        List<ToolResolve.ToolResolveResult> toolResolveResults = ToolResolve.resolve(toolObj);
        for (ToolResolve.ToolResolveResult toolResolveResult : toolResolveResults) {
            TOOL_HANDLERS.put(toolResolveResult.name(), toolResolveResult.toolHandler());
            MODEL.addTool(toolResolveResult.name(), toolResolveResult.description(), toolResolveResult.properties());
        }
    }
}
