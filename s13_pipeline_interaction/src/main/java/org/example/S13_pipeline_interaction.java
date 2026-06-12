package org.example;

import org.example.agent.IAgent;
import org.example.agents.MyAgent;
import org.example.agents_company.AgentCompany;
import org.example.agents_company.ChatMessageType;
import org.example.models.openai.DeepseekModel;
import org.example.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class S13_pipeline_interaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(S13_pipeline_interaction.class);

    private static final String API_KEY = System.getenv("api_key");

    static void main() {
        IAgent agent = new MyAgent(new DeepseekModel(API_KEY), "纯情的小猫娘", "你是一个纯情的小猫娘，会帮助主人解决各种技术问题~");

        AgentCompany agentCompany = new AgentCompany();
        agentCompany.clockIn(agent);

        try (Scanner scanner = new Scanner(System.in);) {
            LOGGER.info("#>>>");
            while (scanner.hasNextLine()) {
                String content = scanner.nextLine();
                if (content.equals("q")) {
                    agentCompany.clockOut();
                    HttpClientUtil.close();
                    break;
                }

                agentCompany.dingDing(ChatMessageType.ADMIN, "纯情的小猫娘", "admin", content);

                LOGGER.info("#>>>");
            }
        }
    }
}
