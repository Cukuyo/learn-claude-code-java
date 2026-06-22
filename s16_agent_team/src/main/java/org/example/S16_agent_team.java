package org.example;

import org.example.agent.IAgent;
import org.example.agents.MyAgent;
import org.example.agents_company.AgentCompany;
import org.example.models.openai.OpenAiModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class S16_agent_team {
    private static final Logger LOGGER = LoggerFactory.getLogger(S16_agent_team.class);

    public static final String URL = "https://api.deepseek.com/chat/completions";
    private static final String API_KEY = System.getenv("api_key");
    public static final String MODEL = "deepseek-v4-flash";

    static void main() {
        IAgent chocola = getAgent("巧克力",
                                  "你是一只猫娘，属于黑白混血短毛猫，香草的双胞胎姐姐。熟练掌握架构、编程等计算机知识");
        IAgent vanilla = getAgent("香草", "你是一只猫娘，属于纯白混种猫，巧克力双胞胎妹妹。熟练掌握架构、编程等计算机知识");
        IAgent azuki = getAgent("红豆", "你是一只猫娘，属于曼基康短腿猫。熟练掌握架构、编程等计算机知识");
        IAgent coconut = getAgent("椰子", "你是一只猫娘，属于缅因猫。熟练掌握架构、编程等计算机知识");
        IAgent maple = getAgent("枫", "你是一只猫娘，属于美国卷耳猫。熟练掌握架构、编程等计算机知识");
        IAgent cinnamon = getAgent("桂", "你是一只猫娘，属于苏格兰折耳猫。熟练掌握架构、编程等计算机知识");

        AgentCompany agentCompany = new AgentCompany("猫娘咖啡馆");
        agentCompany.clockIn(chocola);

        try (Scanner scanner = new Scanner(System.in)) {
            LOGGER.info("#>>>");
            while (scanner.hasNextLine()) {
                String content = scanner.nextLine();
                if (content.equals("q")) {
                    break;
                }

                String rspContent = agentCompany.dingDingByAdmin("巧克力", System.getProperty("user.name"), content);
                LOGGER.info("{} -> {} : {}", "巧克力", System.getProperty("user.name"), rspContent);
                LOGGER.info("#>>>");
            }
        }

        agentCompany.close();
    }

    private static MyAgent getAgent(String name, String agentRole) {
        return new MyAgent(new OpenAiModel(URL, API_KEY, MODEL), name, agentRole);
    }
}
