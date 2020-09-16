package com.test.mybatis.config;

import java.util.concurrent.*;

/**
 * 安全的定时线程池
 */
public class SafeScheduleThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    private final int queueCapacity;

    public SafeScheduleThreadPoolExecutor(int corePoolSize,
                                          int maximumPoolSize,
                                          int queueCapacity,
                                          ThreadFactory threadFactory,
                                          RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
        setMaximumPoolSize(maximumPoolSize);
        this.queueCapacity = queueCapacity;
    }

    public SafeScheduleThreadPoolExecutor(int corePoolSize,
                                          int maximumPoolSize,
                                          int queueCapacity,
                                          long keepAliveTime,
                                          TimeUnit unit,
                                          ThreadFactory threadFactory,
                                          RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
        setMaximumPoolSize(maximumPoolSize);
        setKeepAliveTime(keepAliveTime, unit);
        this.queueCapacity = queueCapacity;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if(getQueue().size() >= queueCapacity) {
            getRejectedExecutionHandler().rejectedExecution(command, this);
        }
        return super.schedule(command, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        if(getQueue().size() >= queueCapacity) {
            getRejectedExecutionHandler().rejectedExecution(new FutureTask<>(callable), this);
        }
        return super.schedule(callable, delay, unit);
    }


}
