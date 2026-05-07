package org.example.use_agents;

import org.example.define_agent.IAgent;
import org.example.define_agent.core.AbstractAgent;
import org.example.define_agent.core.AgentLoopAgent;
import org.example.define_agent.core.components.SkillUseComponent;
import org.example.define_models.AbstractModel;
import org.example.use_agents.extra.AgentLogPrintSupport;
import org.example.use_agents.extra.ContextSummarySupport;
import org.example.use_agents.extra.PermissionSystem;
import org.example.use_agents.extra.TodoManagerSupport;
import org.example.use_agents.extra.ToolUseCompactSupport;
import org.example.use_tools.cmd.AgentCommandTool;
import org.example.use_tools.file.AgentFileTool;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * 子agent，封装了SkillUseAgent，添加了:
 * 1、支持命令行工具
 * 2、支持文件编辑工具
 * 3、支持skills
 * <p>
 * 4、支持todoManager
 * 5、支持上下文压缩
 * 6、支持权限管控
 */
public class SubAgent implements IAgent {
    /**
     * 创建一个单独的子agent执行任务
     *
     * @param model         模型
     * @param agentName     agentName
     * @param chatOrCommand 提示词
     * @return llm 返回
     */
    public static String singleChat(AbstractModel model, String agentName, String chatOrCommand) throws IOException, InterruptedException {
        return new SubAgent(model.cloneWithoutHistory(), agentName).chatOrCommand(chatOrCommand);
    }

    protected final AbstractAgent agent;

    public SubAgent(AbstractModel model, String agentName) throws IOException {
        agent = new AgentLoopAgent(model, agentName);

        agent.registryTool(AgentCommandTool.class);
        agent.registryTool(AgentFileTool.class);

        agent.registrySkills(System.getProperty("user.dir") + File.separator + "skills");

        agent.registryAgentCallback(AgentLogPrintSupport.INSTANCE);
        agent.registryAgentCallback(new TodoManagerSupport());
        agent.registryAgentCallback(new ToolUseCompactSupport(10));
        agent.registryAgentCallback(new ContextSummarySupport(0.5d, 3));

        PermissionSystem permissionSystem;
        try {
            permissionSystem = new PermissionSystem(Paths.get(getClass().getClassLoader().getResource("permission_deny.properties").toURI()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        agent.registryHook(permissionSystem);
        agent.registryCommand(permissionSystem);
    }

    @Override
    public AbstractModel getModel() {
        return agent.getModel();
    }

    @Override
    public String getAgentName() {
        return agent.getAgentName();
    }

    @Override
    public String chatOrCommand(String content) throws IOException, InterruptedException {
        return agent.chatOrCommand(content);
    }
}
