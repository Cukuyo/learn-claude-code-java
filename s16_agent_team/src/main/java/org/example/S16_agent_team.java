package org.example;

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
        AgentCompany agentCompany = new AgentCompany("猫娘互联网科技部", "技术宅拯救世界");
        agentCompany.clockIn(getAgent("巧克力",
                        "你是一只猫娘，属于黑白混血短毛猫，香草的双胞胎姐姐"),
                "项目经理",
                "统筹项目全流程，管控进度、需求、成本，协调研发测试运维，跟进风险与交付验收，向上同步进度");
        agentCompany.clockIn(getAgent("香草", "你是一只猫娘，属于纯白混种猫，巧克力双胞胎妹妹"),
                "架构师",
                "整体架构设计，技术选型，制定规范，评审方案，搭建高可用架构，攻克技术难题，优化系统性能");
        agentCompany.clockIn(getAgent("红豆", "你是一只猫娘，属于曼基康短腿猫"),
                "开发",
                "根据方案编码开发，编写 SQL 与单元测试，联调修复缺陷，维护开发文档，排查线上问题");
        agentCompany.clockIn(getAgent("椰子", "你是一只猫娘，属于缅因猫"),
                "测试",
                "输出测试方案、测试用例，执行版本测试，管控缺陷，出具测试报告");

        try (Scanner scanner = new Scanner(System.in)) {
            LOGGER.info("#>>>");
            while (scanner.hasNextLine()) {
                String content = scanner.nextLine();
                if (content.equals("q")) {
                    break;
                }

                String[] arr = content.split(" ");
                String rspContent = agentCompany.dingDingByAdmin(arr[0], System.getProperty("user.name"), arr[1]);
                LOGGER.info("{} -> {} : {}", System.getProperty("user.name"), arr[0], rspContent);
                LOGGER.info("#>>>");
            }
        }

        agentCompany.close();
    }

    private static MyAgent getAgent(String name, String agentRole) {
        return new MyAgent(new OpenAiModel(URL, API_KEY, MODEL), name, agentRole);
    }
}
