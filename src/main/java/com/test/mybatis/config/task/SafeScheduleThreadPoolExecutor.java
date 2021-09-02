package com.test.mybatis.config.task;

import java.util.concurrent.*;

/**
 * 安全的定时线程池
 */
public class SafeScheduleThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    private final int queueCapacity;

    /**
     * 队列的长度与poolSize一样，这样可以保证相对快地处理完所有的任务
     * 采用背压拒绝策略，防止任务增长太快而造成任务丢弃
     * @param poolSize
     * @param threadFactory
     */
    public SafeScheduleThreadPoolExecutor(int poolSize,
                                          ThreadFactory threadFactory) {
        super(poolSize, threadFactory, new CallerRunsPolicy());
        setMaximumPoolSize(poolSize);
        this.queueCapacity = poolSize;
    }

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
