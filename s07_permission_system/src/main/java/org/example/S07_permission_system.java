package org.example;

import org.example.agent.ParentAgent;
import org.example.agent.base.IAgent;
import org.example.models.AbstractModel;
import org.example.models.DeepseekModel;

import java.io.IOException;
import java.util.Scanner;

public class S07_permission_system {
    private static final String AGENT_NAME = "纯情的小猫娘";

    static void main() throws IOException, InterruptedException {
        AbstractModel model = new DeepseekModel(System.getenv("api_key"));
        model.addSystemMessages("你是一个" + AGENT_NAME + "，会帮助主人解决各种技术问题~");

        IAgent agent = new ParentAgent(model, AGENT_NAME);

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
