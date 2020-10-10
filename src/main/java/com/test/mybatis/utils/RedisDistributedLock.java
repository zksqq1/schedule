package com.test.mybatis.utils;

import com.test.mybatis.config.task.SafeScheduleThreadPoolExecutor;
import com.test.mybatis.exception.CustomException;
import org.apache.lucene.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * redis实现的分布式锁
 */
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisDistributedLock extends DistributedLock {
    private static ScheduledThreadPoolExecutor executor = new SafeScheduleThreadPoolExecutor(10, new NamedThreadFactory("distributedLock-"), new ThreadPoolExecutor.CallerRunsPolicy());
    private static ConcurrentHashMap<String, Integer> lockCountMap = new ConcurrentHashMap<>();
    private static ThreadLocal<String> currentThreadId = new ThreadLocal<>();
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean tryLock(String key, long millSeconds) throws CustomException {
        String threadId = currentThreadId.get() == null ? UUID.randomUUID().toString().replace("-", "") : currentThreadId.get();
        if(threadId != null) {
            String existsThreadId = redisTemplate.opsForValue().get(key);
            if(existsThreadId != null) {
                if (Objects.equals(threadId, existsThreadId)) {
                    redisTemplate.opsForValue().set(key, existsThreadId, millSeconds, TimeUnit.MILLISECONDS);
                    lockCountMap.computeIfPresent(key, (k, v) -> lockCountMap.getOrDefault(k, 0) + 1);
                    return true;
                } else if (!StringUtils.isEmpty(existsThreadId)) {
                    throw new CustomException("已被占用，获取锁失败");
                }
            }
        }
        Boolean absent = redisTemplate.opsForValue().setIfAbsent(key, threadId, millSeconds, TimeUnit.MILLISECONDS);
        if(absent) {
            Runnable runnable = () -> {
                Long expire = redisTemplate.getExpire(key, TimeUnit.MICROSECONDS);
                if (expire != null && expire > 0) {
                    redisTemplate.expireAt(key, new Date(System.currentTimeMillis() + millSeconds));
                }
            };
            executor.schedule(runnable, millSeconds / 3, TimeUnit.MILLISECONDS);
            //设置这个锁的持有者是当前线程
            if(currentThreadId.get() == null) {
                currentThreadId.set(threadId);
                lockCountMap.put(threadId, 1);
            } else {
                lockCountMap.computeIfPresent(key, (k, v) -> lockCountMap.getOrDefault(k, 0) + 1);
            }
        }
        return absent;
    }

    @Override
    public void releaseLock(String key) throws CustomException {
        String threadId = redisTemplate.opsForValue().get(key);
        if(threadId != null) {
            if (Objects.equals(currentThreadId.get(), threadId)) {
                Integer times = lockCountMap.getOrDefault(key, 0);
                int remaining = times - 1;
                if (remaining > 0) {
                    lockCountMap.put(key, remaining);
                    Long expire = redisTemplate.getExpire(key, TimeUnit.MICROSECONDS);
                    redisTemplate.opsForValue().set(key, threadId, expire, TimeUnit.MILLISECONDS);
                } else if(remaining == 0) {
                    redisTemplate.delete(key);
                    currentThreadId.remove();
                    lockCountMap.remove(threadId);
                } else {
                    throw new CustomException("无效的释放锁操作");
                }
            }
        }
        throw new CustomException("无效的释放锁操作");
    }
}
