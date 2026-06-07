package org.example.agents.systems;

import org.example.framework_agent.AgentCallback;
import org.example.framework_agent.AgentCommand;
import org.example.framework_agent.core.AbstractAgent;
import org.example.framework_systems.memory.MemoryDirUtil;
import org.example.framework_systems.memory.MemoryEntity;
import org.example.framework_systems.memory.MemoryFileUtil;
import org.example.framework_systems.memory.MemoryType;
import org.example.framework_tool.ToolMethod;
import org.example.framework_tool.ToolParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * 基于文件系统的记忆管理器：对齐Python的MemoryManager
 */
public class MemorySystem implements AgentCallback, AgentCommand {
    private final Path memoryDirPath;

    public MemorySystem(Path memoryDirPath) {
        this.memoryDirPath = memoryDirPath;
    }

    @Override
    public void initSelf(AbstractAgent agent) {
        agent.registryTool(this);

        if (!Files.exists(memoryDirPath)) {
            memoryDirPath.toFile().mkdirs();
        }

        List<MemoryEntity> memoryList = MemoryDirUtil.resolveDir(memoryDirPath);
        memoryList.sort(Comparator.comparing(o -> o.type));

        renderPrompts(agent, memoryList);
    }

    private void renderPrompts(AbstractAgent agent, List<MemoryEntity> memoryList) {
        String content = """
                                 [MemorySystem]记忆系统用于对关键信息进行跨会话的保存和加载，使用<saveMemory>保存记忆。
                                  需注意：
                                  何时需要保存记忆：
                                  -用户表达个人偏好（如「我习惯用标签页」「一律使用 pytest」）→ 类型：用户偏好-USER
                                  -用户对你进行纠正（如「不要这样做」「刚才的做法有误，原因是……」）→ 类型：反馈修正-FEEDBACK
                                  -获知仅靠现有代码难以自行推断的项目既定规则（例如：因合规要求必须遵守某项规范、某老旧模块出于业务原因严禁改动）→ 类型：项目规则-PROJECT
                                  -获知外部资源的存放地址（工单看板、数据仪表盘、文档链接）→ 类型：参考资源-REFERENCE
                                  无需保存的内容：
                                  -从代码中可直接推导的信息（函数签名、文件结构、目录布局）
                                  -临时任务状态（当前代码分支、待合并 PR 编号、临时待办事项）
                                  -隐私密钥与凭证（API 密钥、账号密码等敏感信息）
                                  当前已加载的记忆简介如下，可使用<saveMemory>保存新记忆或覆盖旧记忆，当需要时根据路径查看文件获取详细内容：
                                 """ + buildMemories(memoryList);

        agent.getModel().addSystemMessages(content);
    }

    private String buildMemories(List<MemoryEntity> memoryList) {
        StringBuilder builder = new StringBuilder(memoryList.size() * 512);
        if (memoryList.isEmpty()) {
            builder.append("当前历史记忆为空");
            return builder.toString();
        }

        memoryList.forEach(memoryEntity -> builder.append(memoryEntity.toPrompt()).append(System.lineSeparator()));

        return builder.toString();
    }

    @ToolMethod(description = "保存新记忆或覆盖旧记忆")
    public String saveMemory(@ToolParam(description = "关键信息的类型") MemoryType type,
                             @ToolParam(description = "关键信息的名称，采用驼峰加下划线的形式") String name,
                             @ToolParam(description = "关键信息的简短描述") String description,
                             @ToolParam(description = "关键信息的全部内容") String content) {
        MemoryEntity memoryEntity = new MemoryEntity();
        memoryEntity.type = type;
        memoryEntity.name = name;
        memoryEntity.description = description;
        memoryEntity.content = content;
        try {
            MemoryFileUtil.write(memoryDirPath, memoryEntity);
            return String.format("<%s><%s>记忆已成功写入!", type, name);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public boolean isSupportCommand(AbstractAgent agent, String cmd) {
        return cmd.trim().split("\\s+")[0].equals("/memories");
    }

    @Override
    public String command(AbstractAgent agent, String cmd) {
        List<MemoryEntity> memoryList = MemoryDirUtil.resolveDir(memoryDirPath);
        memoryList.sort(Comparator.comparing(o -> o.type));
        return buildMemories(memoryList);
    }
}
