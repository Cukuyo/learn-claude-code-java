package org.example.agents.systems;

import org.example.framework_agent.AgentCallback;
import org.example.framework_agent.core.AbstractAgent;
import org.example.framework_systems.task.TaskDirUtil;
import org.example.framework_systems.task.TaskEntity;
import org.example.framework_systems.task.TaskFileUtil;
import org.example.framework_tool.ToolMethod;
import org.example.framework_tool.ToolParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 基于文件系统的任务管理器
 */
public class TaskSystem implements AgentCallback {
    private final Path taskDirPath;
    private AbstractAgent agent = null;

    public TaskSystem(Path taskDirPath) {
        this.taskDirPath = taskDirPath;
    }

    @Override
    public void initSelf(AbstractAgent agent) {
        this.agent = agent;
        agent.registryTool(this);

        if (!Files.exists(taskDirPath)) {
            taskDirPath.toFile().mkdirs();
        }

        List<TaskEntity> taskList = TaskDirUtil.resolveDir(taskDirPath);
        List<TaskEntity> waitedTaskList = taskList.stream().filter(
                task -> task.agent.equals(agent.agentName) && task.progress < 100).toList();

        renderPrompts(agent, waitedTaskList);
    }

    private void renderPrompts(AbstractAgent agent, List<TaskEntity> taskList) {
        agent.getModel().addUserMessage("""
                                                [TaskSystem]任务系统用于对任务进行跨会话的保存和加载，使用<updateTasks>可对任务进行保存。需注意：
                                                1、进行多步骤任务时必须使用TaskSystem进行保存
                                                2、必须使用Mermaid格式进行保存，任务间显示声明依赖关系和完成情况
                                                当前已加载的未完成历史任务如下：
                                                """ + loadTasks(taskList));
    }

    private String loadTasks(List<TaskEntity> taskList) {
        StringBuilder builder = new StringBuilder(taskList.size() * 512);
        if (taskList.isEmpty()) {
            builder.append("当前历史任务为空");
            return builder.toString();
        }
        for (TaskEntity taskEntity : taskList) {
            builder.append(taskEntity.toPrompt()).append(System.lineSeparator());
        }

        return builder.toString();
    }

    @ToolMethod(description = "使用任务系统保存跨会话的任务信息")
    public String updateTasks(
            @ToolParam(description = "任务名") String name,
            @ToolParam(description = "任务简短描述") String description,
            @ToolParam(description = "Mermaid格式的任务内容") String content,
            @ToolParam(description = "任务进度，0-100") int progress) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.agent = agent.agentName;
        taskEntity.name = name;
        taskEntity.description = description;
        taskEntity.progress = progress;
        taskEntity.content = content;
        try {
            return TaskFileUtil.write(taskDirPath, taskEntity);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
