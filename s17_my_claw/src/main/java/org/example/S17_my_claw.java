package org.example;


import org.example.agent.IAgent;
import org.example.agents.MyAgent;
import org.example.models.openai.OpenAiModel;

import java.util.Scanner;

public class S17_my_claw {
    public static final String URL = "https://api.deepseek.com/chat/completions";
    private static final String API_KEY = System.getenv("api_key");
    public static final String MODEL = "deepseek-v4-flash";

    static void main() {
        IAgent agent = new MyAgent(new OpenAiModel(URL, API_KEY, MODEL), "纯情的小猫娘", "你是通用AI助手，会帮助主人解决各种问题");

        try (Scanner scanner = new Scanner(System.in);) {
            System.out.print("#>>>");
            while (scanner.hasNextLine()) {
                String cmd = scanner.nextLine();
                if (cmd.equals("q")) {
                    break;
                }

                try {
                    agent.chatOrCommand(cmd);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.print("#>>>");
            }
        }
    }
}
