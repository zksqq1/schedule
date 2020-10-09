package com.test.mybatis.utils;

import com.test.mybatis.config.task.SafeScheduleThreadPoolExecutor;
import org.apache.lucene.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * redis实现的分布式锁
 */
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisDistributedLock extends DistributedLock {
    private static ScheduledThreadPoolExecutor executor = new SafeScheduleThreadPoolExecutor(10, new NamedThreadFactory("distributedLock-"), new ThreadPoolExecutor.CallerRunsPolicy());
    private static ThreadLocal<String> currentThreadId = new ThreadLocal<>();
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean tryLock(String key, long millSeconds) {
        String threadId = currentThreadId.get();
        if(threadId != null) {
            String value = redisTemplate.opsForValue().get(key);
            String[] split = value.split("-");
            if(Objects.equals(threadId, split[0])) {
                value = split[0] + (Integer.parseInt(split[1]) + 1);
                redisTemplate.opsForValue().set(key, value, millSeconds, TimeUnit.MILLISECONDS);
                return true;
            }
        }
        String value = UUID.randomUUID().toString().replace("-", "");
        Boolean absent = redisTemplate.opsForValue().setIfAbsent(key, (value  + "-1"), millSeconds, TimeUnit.MILLISECONDS);
        if(absent) {
            Runnable runnable = () -> {
                Long expire = redisTemplate.getExpire(key, TimeUnit.MICROSECONDS);
                if (expire != null && expire > 0) {
                    redisTemplate.expireAt(key, new Date(System.currentTimeMillis() + millSeconds));
                }
            };
            executor.schedule(runnable, millSeconds / 3, TimeUnit.MILLISECONDS);
            //设置这个锁的持有者是当前线程
            currentThreadId.set(value);
        }
        return absent;
    }

    @Override
    public boolean releaseLock(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if(value != null) {
            String[] split = value.split("-");
            if (Objects.equals(currentThreadId.get(), split[0])) {
                int times = Integer.parseInt(split[1]);
                if (times > 1) {
                    Long expire = redisTemplate.getExpire(key, TimeUnit.MICROSECONDS);
                    redisTemplate.opsForValue().set(key, split[0] + (times - 1), expire, TimeUnit.MILLISECONDS);
                } else {
                    redisTemplate.delete(key);
                    currentThreadId.remove();
                }
                return true;
            }
        }
        return false;
    }
}
