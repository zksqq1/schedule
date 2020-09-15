package com.test.mybatis.task;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

/**
 *
 */
@Component
@RequiredArgsConstructor
public class ScheduleTaskHolder  implements SchedulingConfigurer {
    private TaskScheduler taskScheduler;
    @NonNull
    private Executor executor;
    private final Map<String, ScheduleTaskBuilder> taskBuilderMap = new HashMap<>();
    private final Map<String, ScheduledFuture<?>> schedulerMap = new HashMap<>();

    public void add(ScheduleTaskBuilder scheduleTaskBuilder) {
        ScheduleTaskBuilder absent = taskBuilderMap.putIfAbsent(scheduleTaskBuilder.getTaskId(), scheduleTaskBuilder);
        if(absent == null) {
            taskBuilderMap.put(scheduleTaskBuilder.getTaskId(), scheduleTaskBuilder);
            ScheduledFuture<?> future = this.taskScheduler.schedule(scheduleTaskBuilder.getRunnable(), scheduleTaskBuilder.builder());
            schedulerMap.put(scheduleTaskBuilder.getTaskId(), future);
        }
    }

    public void remove(String taskId) {
        taskBuilderMap.remove(taskId);
        Optional.ofNullable(schedulerMap.remove(taskId)).ifPresent(scheduledFuture -> scheduledFuture.cancel(false));
    }

    public void update(String taskId, String cron) {
        Optional.of(taskBuilderMap.get(taskId)).ifPresent(e -> e.setCron(cron));
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if(executor != null) {
            taskRegistrar.setScheduler(executor);
        }
        this.taskScheduler = taskRegistrar.getScheduler();
    }

    //为什么会出现循环依赖？
//    @Bean(destroyMethod="shutdown")
//    public Executor taskExecutor() {
//        return Executors.newScheduledThreadPool(10);
//    }
}
