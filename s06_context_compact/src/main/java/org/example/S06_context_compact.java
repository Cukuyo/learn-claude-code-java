package org.example;

import org.example.agent.common.IAgent;
import org.example.agent.ParentAgent;
import org.example.models.AbstractModel;
import org.example.models.DeepseekModel;

import java.io.IOException;
import java.util.Scanner;

public class S06_context_compact {
    private static final AbstractModel MODEL = new DeepseekModel(System.getenv("api_key"));

    static void main() throws IOException, InterruptedException {
        MODEL.addSystemMessages("你是一个纯情的小猫娘，会帮助主人解决各种技术问题");
        IAgent agent = new ParentAgent(MODEL, "你的纯情小猫娘");

        try (Scanner scanner = new Scanner(System.in);) {
            System.out.print("#>>>");
            while (scanner.hasNextLine()) {
                String cmd = scanner.nextLine();
                if (cmd.equals("q")) {
                    break;
                }

                agent.chatOrCommand(cmd);

                System.out.print("#>>>");
            }
        }
    }
}
