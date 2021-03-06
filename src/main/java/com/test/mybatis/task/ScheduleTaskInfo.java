package com.test.mybatis.task;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

/**
 * 可以扩充属性
 */
@RequiredArgsConstructor
@Data
public class ScheduleTaskInfo {
    @NonNull
    private String taskId;
    @NonNull
    private String cron;
    @NonNull
    private Runnable runnable;

    public Trigger buildTrigger() {
        return triggerContext ->  new CronTrigger(cron).nextExecutionTime(triggerContext);
    }
}
