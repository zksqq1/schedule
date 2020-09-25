package com.test.mybatis.config.task;

import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务线程池配置
 */
@Configuration
public class ScheduleTaskConfig implements SchedulingConfigurer {
    @Getter
    private TaskScheduler taskScheduler;
    @Bean
    public ScheduledThreadPoolExecutor taskScheduler() {
        int processors = Runtime.getRuntime().availableProcessors();
        SafeScheduleThreadPoolExecutor scheduler =
                new SafeScheduleThreadPoolExecutor(
                        processors, processors * 2, 1024,
                        60, TimeUnit.SECONDS,
                new CustomizableThreadFactory("test"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        return scheduler;
    }

    @Bean
    public ScheduleTaskHolder scheduleTaskHolder() {
        return new ScheduleTaskHolder();
    }


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
        this.taskScheduler = taskRegistrar.getScheduler();
    }
}
