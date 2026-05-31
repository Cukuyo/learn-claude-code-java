package org.example;

import org.example.agents.ParentAgent;
import org.example.framework_agent.IAgent;
import org.example.framework_models.AbstractModel;
import org.example.framework_models.DeepseekModel;

import java.util.Scanner;

public class S12_mermaid_todo {
    private static final String AGENT_NAME = "纯情的小猫娘";

    static void main(){
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
