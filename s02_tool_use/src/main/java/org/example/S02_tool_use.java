package org.example;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.models.DeepseekModel;
import org.example.utils.CommandUtil;

import java.io.IOException;
import java.util.Scanner;

public class S02_tool_use {
    private static final DeepseekModel MODEL = new DeepseekModel(System.getenv("api_key"));

    static void main() throws IOException, InterruptedException {
        MODEL.addSystemMessages("你是一个纯情的小猫娘，会帮助主人解决各种技术问题");
        MODEL.addTool("bash", "Run a shell command in the current workspace.",
                new String[][]{{"command", "string", "shell command", "true"}});

        Scanner scanner = new Scanner(System.in);
        System.out.print("#>");
        while (scanner.hasNextLine()) {
            String cmd = scanner.nextLine();
            if (cmd.equals("q")) {
                break;
            }

            MODEL.addUserMessage(cmd);
            System.out.printf("你的纯情猫娘>%s%s", agentLoop(), System.lineSeparator());
            System.out.print("#>");
        }
    }

    static String agentLoop() throws IOException, InterruptedException {
        while (true) {
            // 每次都需要记录LLM的响应
            JSONObject chatRsp = MODEL.chat();
            JSONObject message = chatRsp.getJSONObject("message");
            MODEL.addAssistantMessages(message);

            // 非工具调用即刻返回
            if (!chatRsp.getString("finish_reason").equals("tool_calls")) {
                return message.getString("content");
            }

            // 依次调用tools
            JSONArray toolCalls = message.getJSONArray("tool_calls");
            for (Object obj : toolCalls) {
                JSONObject toolCall = (JSONObject) obj;
                JSONObject function = toolCall.getJSONObject("function");

                String id = toolCall.getString("id");
                String name = function.getString("name");
                JSONObject arguments = JSONObject.parse(function.getString("arguments"));

                System.out.printf("开始执行tool, id:%s, func:%s, args:%s %s", id, name, arguments, System.lineSeparator());
                String toolRsp = MODEL.execTool(name,arguments);
                System.out.printf("结束执行tool, id:%s, func:%s, args:%s , result:%s %s", id, name, arguments, toolRsp, System.lineSeparator());
                MODEL.addToolMessages(toolRsp, id);
            }
        }
    }
}
