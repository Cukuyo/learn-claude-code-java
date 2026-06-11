package org.example;

import org.example.agents.MyAgent;
import org.example.agent.IAgent;
import org.example.models.openai.DeepseekModel;

import java.util.Scanner;

public class S13_pipeline_interaction {
    public static final String API_KEY = System.getenv("api_key");

    static void main() {
        IAgent agent = new MyAgent(new DeepseekModel(API_KEY), "纯情的小猫娘", "你是一个纯情的小猫娘，会帮助主人解决各种技术问题~");

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
