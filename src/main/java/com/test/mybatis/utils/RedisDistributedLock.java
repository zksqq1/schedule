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
import java.util.concurrent.*;

/**
 * redis实现的分布式锁
 * 在集群情况下，如果主Redis挂掉，而锁信息未同步到从Redis。此时从节点被选为主节点时，会存在锁丢失的问题
 */
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisDistributedLock extends DistributedLock {
    //用来定期延长锁失效时间的线程池
    private static final ScheduledThreadPoolExecutor executor = new SafeScheduleThreadPoolExecutor(10, 20, 200, new NamedThreadFactory("distributedLock-"), new ThreadPoolExecutor.CallerRunsPolicy());
    private static final ConcurrentHashMap<String, Integer> lockCountMap = new ConcurrentHashMap<>();
    private static final ThreadLocal<String> currentThreadId = new ThreadLocal<>();
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean tryLock(String key, long millSeconds) throws CustomException {
        String threadId = currentThreadId.get() == null ? UUID.randomUUID().toString().replace("-", "") : currentThreadId.get();
        String existsThreadId = redisTemplate.opsForValue().get(key);
        if(existsThreadId != null) {
            if (Objects.equals(threadId, existsThreadId)) {
                redisTemplate.opsForValue().set(key, existsThreadId, millSeconds, TimeUnit.MILLISECONDS);
                lockCountMap.computeIfPresent(key, (k, v) -> lockCountMap.getOrDefault(k, 0) + 1);
                return true;
            } else {
                throw new CustomException("锁已被占用，获取失败");
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
            //记录锁的持有者信息到当前线程本地变量中
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
