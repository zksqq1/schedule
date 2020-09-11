package com.test.mybatis.task;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

/**
 *
 */
@RequiredArgsConstructor
@Data
public class ScheduleTaskBuilder {
    @NonNull
    private String taskId;
    @NonNull
    private String cron;
    @NonNull
    private Runnable runnable;

    public Trigger builder() {
        return triggerContext -> new CronTrigger(cron).nextExecutionTime(triggerContext);
    }
}
