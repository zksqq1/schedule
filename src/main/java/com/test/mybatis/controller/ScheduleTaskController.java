package com.test.mybatis.controller;

import com.test.mybatis.task.ScheduleTaskBuilder;
import com.test.mybatis.task.ScheduleTaskHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/cron/add")
    public void add() {
        taskHolder.add(new ScheduleTaskBuilder("test1", "0/1 * * * * ?", () -> System.out.println(System.currentTimeMillis() + "test1")));
    }
}
