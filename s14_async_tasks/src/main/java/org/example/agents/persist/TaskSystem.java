package org.example.agents.persist;

import org.example.agent.AgentCallback;
import org.example.agent.impl.AbstractAgent;
import org.example.agents.persist.systems.task.TaskDirUtil;
import org.example.agents.persist.systems.task.TaskEntity;
import org.example.agents.persist.systems.task.TaskFileUtil;
import org.example.agent.tool.ToolMethod;
import org.example.agent.tool.ToolParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 基于文件系统的任务管理器
 */
public class TaskSystem implements AgentCallback {
    private final Path taskDirPath;

    public TaskSystem(Path taskDirPath) {
        this.taskDirPath = taskDirPath;
    }

    @Override
    public void initSelf(AbstractAgent agent) {
        agent.registryTool(this);

        if (!Files.exists(taskDirPath)) {
            taskDirPath.toFile().mkdirs();
        }

        List<TaskEntity> taskList = TaskDirUtil.resolveDir(taskDirPath);
        List<TaskEntity> waitedTaskList = taskList.stream().filter(task -> task.progress < 100).toList();

        renderPrompts(agent, waitedTaskList);
    }

    private void renderPrompts(AbstractAgent agent, List<TaskEntity> taskList) {
        agent.getModel().addUserMessage("""
                [TaskSystem]任务系统用于对任务进行跨会话的保存和加载。
                需注意：
                1、进行多步骤任务时必须使用TaskSystem进行保存，防止遗忘
                2、必须使用Mermaid格式进行保存，任务间显示声明依赖关系和完成情况
                当前已加载的未完成历史任务如下，可使用<updateTasks>对任务进行新保存或覆盖：
                """ + buildTasks(taskList));
    }

    private String buildTasks(List<TaskEntity> taskList) {
        StringBuilder builder = new StringBuilder(taskList.size() * 512);
        if (taskList.isEmpty()) {
            builder.append("当前历史任务为空");
            return builder.toString();
        }

        taskList.forEach(taskEntity -> builder.append(taskEntity.toPrompt()).append(System.lineSeparator()));

        return builder.toString();
    }

    @ToolMethod(description = "对任务进行新保存或覆盖")
    public String updateTasks(
            @ToolParam(description = "agent名字，也就是你的名字") String agentName,
            @ToolParam(description = "任务名，采用驼峰加下划线的形式") String name,
            @ToolParam(description = "任务的简短描述") String description,
            @ToolParam(description = "Mermaid格式的任务内容") String content,
            @ToolParam(description = "任务进度，0-100") int progress) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.agentName = agentName;
        taskEntity.name = name;
        taskEntity.description = description;
        taskEntity.progress = progress;
        taskEntity.content = content;
        try {
            TaskFileUtil.write(taskDirPath, taskEntity);
            return String.format("<%s>已成功写入!", name);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
