package com.test.mybatis.utils;

import com.test.mybatis.config.task.SafeScheduleThreadPoolExecutor;
import com.test.mybatis.exception.CustomException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.*;

/**
 * redis实现的分布式锁
 * 在集群情况下，如果主Redis挂掉，而锁信息未同步到从Redis。此时从节点被选为主节点时，会存在锁丢失的问题
 */
@Component
@RequiredArgsConstructor
public class RedisDistributedLock extends DistributedLock {
    //用来定期延长锁失效时间的线程池
    private static final ScheduledThreadPoolExecutor executor = new SafeScheduleThreadPoolExecutor(10, 20, 200, new NamedThreadFactory("distributedLock-"), new ThreadPoolExecutor.CallerRunsPolicy());
    private static final ConcurrentHashMap<String, Integer> lockCountMap = new ConcurrentHashMap<>();
    private static final Map<String, ScheduledFuture<?>> renewedKey = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> holderKeyMap = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean tryLock(String key, long millSeconds, String holderId) throws CustomException {
        Assert.hasText(holderId, "holder不能为空");
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        String existsHolderId = redisTemplate.opsForValue().get(key);
        if(existsHolderId != null) {
            if (Objects.equals(holderId, existsHolderId)) {
                redisTemplate.opsForValue().setIfPresent(key, existsHolderId, millSeconds, timeUnit);
                lockCountMap.computeIfPresent(key, (k, v) -> lockCountMap.getOrDefault(k, 0) + 1);
                return true;
            } else {
                throw new CustomException("锁已被占用，获取失败");
            }
        }
        Boolean absent = redisTemplate.opsForValue().setIfAbsent(key, holderId, millSeconds, timeUnit);
        if(absent) {
            Runnable runnable = () -> {
                Long expire = redisTemplate.getExpire(key, timeUnit);
                if (expire != null && expire > 0) {
                    redisTemplate.expireAt(key, new Date(System.currentTimeMillis() + millSeconds));
                }
            };
            if(renewedKey.containsKey(key)) {
                renewedKey.get(key).cancel(false);
            }
            ScheduledFuture<?> future = executor.scheduleAtFixedRate(runnable, 0, millSeconds / 3, timeUnit);
            renewedKey.put(key, future);
            lockCountMap.put(key, lockCountMap.getOrDefault(key, 0) + 1);
            Optional.ofNullable(holderKeyMap.get(holderId)).orElseGet(() -> {
                HashSet<String> hashSet = new HashSet<>();
                holderKeyMap.put(holderId, hashSet);
                return hashSet;
            }).add(holderId);
        }
        return absent;
    }

    @Override
    public void releaseLock(String key, String holderId) throws CustomException {
        String existsHolderId = redisTemplate.opsForValue().get(key);
        if(existsHolderId != null) {
            if (Objects.equals(holderId, existsHolderId)) {
                Integer times = lockCountMap.getOrDefault(key, 0);
                int remaining = times - 1;
                if (remaining > 0) {
                    lockCountMap.put(key, remaining);
                    Long expire = redisTemplate.getExpire(key, TimeUnit.MICROSECONDS);
                    redisTemplate.opsForValue().set(key, existsHolderId, expire, TimeUnit.MILLISECONDS);
                } else {
                    Set<String> set = holderKeyMap.get(holderId);
                    set.remove(key);
                    if(set.size() == 0) {
                        holderKeyMap.remove(holderId);
                    }
                    lockCountMap.remove(key);
                    renewedKey.remove(key).cancel(true);
                    redisTemplate.delete(key);
                }
            }
        }
    }
}
