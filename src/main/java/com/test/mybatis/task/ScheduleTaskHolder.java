package com.test.mybatis.task;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

/**
 *
 */
@Component
@RequiredArgsConstructor
public class ScheduleTaskHolder {
    @NonNull
    private TaskScheduler taskScheduler;
    private Map<String, ScheduleTaskBuilder> map = new HashMap<>();
    private Map<String, ScheduledFuture> maps = new HashMap<>();

    @PostConstruct
    public void init() {
        add(new ScheduleTaskBuilder("test", "0/1 * * * * ?", () -> System.out.println(System.currentTimeMillis())));
    }


    public void add(ScheduleTaskBuilder scheduleTaskBuilder) {
        map.put(scheduleTaskBuilder.getTaskId(), scheduleTaskBuilder);
        ScheduledFuture<?> future = taskScheduler.schedule(scheduleTaskBuilder.getRunnable(), scheduleTaskBuilder.builder());
        maps.put(scheduleTaskBuilder.getTaskId(), future);
    }

    public void remove(String taskId) {
        map.remove(taskId);
        maps.remove(taskId).cancel(false);
    }

    public void update(String taskId, String cron) {
        Optional.of(map.get(taskId)).ifPresent(e -> e.setCron(cron));
    }
}
