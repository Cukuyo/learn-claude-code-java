package org.example.agents.systems;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.example.define_agent.AgentCallback;
import org.example.define_agent.AgentCommand;
import org.example.define_agent.core.AbstractAgent;
import org.example.define_systems.memory.MemoryDirUtil;
import org.example.define_systems.memory.MemoryEntity;
import org.example.define_systems.memory.MemoryFileUtil;
import org.example.define_systems.memory.MemoryType;
import org.example.define_tool.ToolMethod;
import org.example.define_tool.ToolParam;

import com.alibaba.fastjson2.JSONObject;

/**
 * 基于文件系统的记忆管理器：对齐Python的MemoryManager
 */
public class MemorySystem implements AgentCallback, AgentCommand{
    private final Path memoryDirPath;
    private Map<String,MemoryEntity> memoryMap = new HashMap<>();
    protected JSONObject memorySystemMessage;

    public MemorySystem(Path memoryDirPath){
        this.memoryDirPath = memoryDirPath;
    }

    @Override
    public void eachAtomicInitFirst(AbstractAgent agent) {
        agent.registryTool(this);

        if (!Files.exists(memoryDirPath)) {
            memoryDirPath.toFile().mkdirs();
        }

        List<MemoryEntity> memoryList = MemoryDirUtil.resolveDir(memoryDirPath);
        memoryList.forEach(cv -> memoryMap.put(cv.name, cv));

        renderPrompts(agent);  
    }

    private void renderPrompts(AbstractAgent agent) {
        if (memorySystemMessage != null) {
            memorySystemMessage.put("content", loadMemories());
        } else {
            memorySystemMessage = agent.model.addSystemMessages(loadMemories());
        }
    }

    private String loadMemories() {
        StringBuilder builder = new StringBuilder(memoryMap.size() * 512);
        builder.append(
        """
            [MemorySystem]记忆系统用于对关键信息进行跨会话的保存和加载，使用<saveMemory>可对记忆进行保存。需注意：
            何时需要保存记忆：
                用户表达个人偏好（如「我习惯用标签页」「一律使用 pytest」）→ 类型：用户偏好-USER
                用户对你进行纠正（如「不要这样做」「刚才的做法有误，原因是……」）→ 类型：反馈修正-FEEDBACK
                获知仅靠现有代码难以自行推断的项目既定规则（例如：因合规要求必须遵守某项规范、某老旧模块出于业务原因严禁改动）→ 类型：项目规则-PROJECT
                获知外部资源的存放地址（工单看板、数据仪表盘、文档链接）→ 类型：参考资源-REFERENCE
            无需保存的内容：
                从代码中可直接推导的信息（函数签名、文件结构、目录布局）
                临时任务状态（当前代码分支、待合并 PR 编号、临时待办事项）
                隐私密钥与凭证（API 密钥、账号密码等敏感信息）
            当前已加载的历史记忆如下：

        """).append(buildMemories());

  
        return builder.toString();
    }

        private String buildMemories(){
        StringBuilder builder = new StringBuilder(memoryMap.size() * 512);
        if (memoryMap.isEmpty()) {
            builder.append("当前历史记忆为空");
            return builder.toString();
        }

        for (MemoryEntity memory : memoryMap.values()) {
                builder.append("- {")
                    .append(memory.type)
                    .append(":").append(memory.name)
                    .append(":").append(memory.content)
                    .append("}").append(System.lineSeparator());
        }
  
        return builder.toString();
    }

    @ToolMethod(description = "使用记忆系统保存跨会话的关键信息")
    public String saveMemory(
        @ToolParam(description = "关键信息的名称，采用驼峰加下划线的形式，与type字段拼接后成为该记忆的完全名字，如USER_codeLike_java") String name,
        @ToolParam(description = "关键信息的简短描述") String description,
        @ToolParam(description = "关键信息的类型") MemoryType type,
        @ToolParam(description = "关键信息的全部内容") String content) {
        MemoryEntity memoryEntity=new MemoryEntity();
        memoryEntity.name=name;
        memoryEntity.description=description;
        memoryEntity.type=type;
        memoryEntity.content=content;
        String result;
        try {
            result= MemoryFileUtil.write(memoryDirPath, memoryEntity);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }

        memoryMap.put(name, memoryEntity);
        renderPrompts(null);

        return result;
    }

    @Override
    public boolean isSupportCommand(AbstractAgent agent, String cmd) {
        return cmd.trim().split("\\s+")[0].equals("/memories");
    }

    @Override
    public String command(AbstractAgent agent, String cmd) throws IOException {
        return buildMemories();
    }
}
