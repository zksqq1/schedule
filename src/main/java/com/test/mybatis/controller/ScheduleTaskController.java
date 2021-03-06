package com.test.mybatis.controller;

import com.test.mybatis.config.task.ScheduleTaskHolder;
import com.test.mybatis.task.ScheduleTaskInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController
@RequiredArgsConstructor
public class ScheduleTaskController {
    @NonNull
    private ScheduleTaskHolder taskHolder;

    @GetMapping("/cron/{taskId}")
    public void update(@PathVariable String taskId,@RequestParam("cron") String cron) {
        taskHolder.update(taskId, cron);
    }

    @GetMapping("/cron/add/{taskId}")
    public void add(@PathVariable String taskId) {
        taskHolder.add(new ScheduleTaskInfo(taskId, "0/1 * * * * ?", () -> {
            System.out.println(Thread.currentThread().getName() + taskId + System.currentTimeMillis());
        }));
    }

    @GetMapping("/cron/del/{taskId}")
    public void del(@PathVariable String taskId) {
        taskHolder.remove(taskId);
    }
}
