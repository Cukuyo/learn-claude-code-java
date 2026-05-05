package org.example.agent.agent_base;

import org.example.agent.*;
import org.example.agent.agent_callbacks.AgentCallback;
import org.example.agent.agent_command.AgentCommand;
import org.example.models.AbstractModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * agent抽象父类:
 * 提供命令输入最开始的判断和部分回调机制
 */
public abstract class AbstractAgent implements
        IAgent, AgentCallback, IAgentToolUse, IAgentSkillUse, IAgentCallBackUse, IAgentHookUse, IAgentCommandUse {
    protected final AbstractModel model;
    protected final String agentName;

    protected final List<AgentCallback> agentCallbacks = new ArrayList<>();
    protected final List<AgentCommand> agentCommands = new ArrayList<>();

    public AbstractAgent(AbstractModel model, String agentName) {
        this.model = model;
        this.agentName = agentName;
    }

    @Override
    public void registryCommand(AgentCommand agentCommand) {
        agentCommands.add(agentCommand);
    }

    @Override
    public String chatOrCommand(String content) throws IOException, InterruptedException {
        eachAtomicInitFirst(this);
        eachCheckSecond(this);
        if (content.startsWith("/")) {
            return command(content);
        } else {
            return agentLoop(content);
        }
    }

    protected String command(String content) {
        Optional<AgentCommand> agentCommandOptional = agentCommands.stream().filter(cv -> cv.isSupportCommand(content)).findFirst();
        return agentCommandOptional.isPresent() ? agentCommandOptional.get().command(content) : "不支持当前命令，请确认后再次输入！";
    }

    /**
     * agentLoop，交给子类实现
     *
     * @param content 用户提示词
     * @return 返回
     */
    protected abstract String agentLoop(String content) throws IOException, InterruptedException;

    @Override
    public AbstractModel getModel() {
        return model;
    }

    @Override
    public String getAgentName() {
        return agentName;
    }

    @Override
    public void registryAgentCallback(AgentCallback agentCallback) {
        agentCallbacks.add(agentCallback);
    }

    @Override
    public void eachAtomicInitFirst(IAgent agent) {
        agentCallbacks.forEach(cv -> cv.eachAtomicInitFirst(agent));
    }

    @Override
    public void eachCheckSecond(IAgent agent) {
        agentCallbacks.forEach(cv -> cv.eachAtomicInitFirst(agent));
    }
}
