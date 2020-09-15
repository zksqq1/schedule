package com.test.mybatis.controller;

import com.test.mybatis.task.ScheduleTaskBuilder;
import com.test.mybatis.task.ScheduleTaskHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 *
 */
@RestController
public class ScheduleTaskController {
    @Autowired
    private ScheduleTaskHolder taskHolder;

    @GetMapping("/cron")
    public void update(@RequestParam("cron") String cron) {
        taskHolder.update("test", cron);
    }

    @GetMapping("/cron/add/{taskId}")
    public void add(@PathVariable String taskId) {
        taskHolder.add(new ScheduleTaskBuilder(taskId, "0/1 * * * * ?", () -> {
            System.out.println(Thread.currentThread().getName() + taskId + System.currentTimeMillis());
        }));
    }

    @GetMapping("/cron/del/{taskId}")
    public void del(@PathVariable String taskId) {
        taskHolder.remove(taskId);
    }
}
