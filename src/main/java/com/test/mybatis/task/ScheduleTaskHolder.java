package com.test.mybatis.task;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
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
    private Map<String, MyScheduleTaskBuilder> map = new HashMap<>();
    private Map<String, ScheduledFuture> maps = new HashMap<>();

    @PostConstruct
    public void init() {
        add(new MyScheduleTaskBuilder("test", "0/1 * * * * ?", () -> System.out.println(System.currentTimeMillis())));
    }


    public void add(MyScheduleTaskBuilder myScheduleTaskBuilder) {
        map.put(myScheduleTaskBuilder.getTaskId(), myScheduleTaskBuilder);
        ScheduledFuture<?> future = taskScheduler.schedule(myScheduleTaskBuilder.getRunnable(), myScheduleTaskBuilder.builder());
        maps.put(myScheduleTaskBuilder.getTaskId(), future);
    }

    public void remove(String taskId) {
        map.remove(taskId);
        maps.remove(taskId).cancel(false);
    }

    public void update(String taskId, String cron) {
        Optional.of(map.get(taskId)).ifPresent(e -> e.setCron(cron));
    }
}
