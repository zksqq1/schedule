package com.test.mybatis.config.task;

import com.test.mybatis.task.ScheduleTaskInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

/**
 *
 */
public class ScheduleTaskHolder {
    @Autowired
    private ScheduleTaskConfig config;

    private final Map<String, ScheduleTaskInfo> taskBuilderMap = new HashMap<>();
    private final Map<String, ScheduledFuture<?>> schedulerMap = new HashMap<>();

    public void add(ScheduleTaskInfo taskInfo) {
        ScheduleTaskInfo absent = taskBuilderMap.putIfAbsent(taskInfo.getTaskId(), taskInfo);
        if(absent == null) {
            taskBuilderMap.put(taskInfo.getTaskId(), taskInfo);
            ScheduledFuture<?> future = config.getTaskScheduler().schedule(taskInfo.getRunnable(), taskInfo.buildTrigger());
            schedulerMap.put(taskInfo.getTaskId(), future);
        }
    }

    public void remove(String taskId) {
        taskBuilderMap.remove(taskId);
        Optional.ofNullable(schedulerMap.remove(taskId)).ifPresent(scheduledFuture -> scheduledFuture.cancel(false));
    }

    public void update(String taskId, String cron) {
        Optional.of(taskBuilderMap.get(taskId)).ifPresent(e -> e.setCron(cron));
    }
}
