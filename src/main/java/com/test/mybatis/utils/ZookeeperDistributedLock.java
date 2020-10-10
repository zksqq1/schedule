package com.test.mybatis.utils;


import com.test.mybatis.exception.CustomException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper实现的分布式锁
 */
@ConditionalOnBean(ZooKeeper.class)
public class ZookeeperDistributedLock extends DistributedLock {
    private static final ThreadLocal<String> currentThreadId = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, Integer> lockCountMap = new ConcurrentHashMap<>();

    @Autowired
    private ZooKeeper zooKeeper;

    @Override
    public boolean tryLock(String key, long millSeconds) throws CustomException {
        CountDownLatch latch = new CountDownLatch(1);
        String createPath = "/" + key;
        String threadId = currentThreadId.get() == null ? UUID.randomUUID().toString().replace("-", "") : currentThreadId.get();
        try {
            Stat exists = zooKeeper.exists(createPath, null);
            if(exists == null) {
                try {
                    zooKeeper.create(createPath, threadId.getBytes(StandardCharsets.UTF_8.name()), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
                            (rc, path, ctx, name, stat) -> {
                                if (stat != null) {
                                    latch.countDown();
                                } else {
                                    throw new CustomException("锁已被占用，获取失败");
                                }
                            }, "");
                    //记录锁的持有者信息到当前线程本地变量中
                    latch.await();
                    currentThreadId.set(threadId);
                    lockCountMap.put(threadId, 1);
                } catch (UnsupportedEncodingException e) {
                    throw new CustomException("获取失败失败", e);
                }
            } else {
                zooKeeper.getData(createPath, false, (rc, path, ctx, data, stat) -> {
                    String existsThreadId = new String(data);
                    if (Objects.equals(threadId, existsThreadId)) {
                        lockCountMap.computeIfPresent(threadId, (k ,v) -> lockCountMap.get(k) + 1);
                    } else {
                        throw new CustomException("锁已被占用，获取失败");
                    }
                }, "");
                latch.countDown();
            }
        } catch (KeeperException | InterruptedException e) {
            throw new CustomException("获取失败失败", e);
        }
        return true;
    }

    @Override
    public void releaseLock(String key) throws CustomException {
        String threadId = currentThreadId.get();
        if(threadId != null) {
            String createPath = "/" + key;
            zooKeeper.getData(createPath, false, (rc, path, ctx, data, stat) -> {
                if (Objects.equals(new String(data), threadId)) {
                    int times = lockCountMap.getOrDefault(threadId, 0);
                    int remaining = times - 1;
                    if (remaining > 0) {
                        try {
                            zooKeeper.setData(createPath, threadId.getBytes(StandardCharsets.UTF_8.name()), -1, (rc1, path1, ctx1, stat1) -> {
                                if (stat1 == null) {
                                    throw new CustomException("释放分布式锁失败");
                                }
                            }, "");
                            lockCountMap.put(threadId, remaining);
                        } catch (UnsupportedEncodingException e) {
                            throw new CustomException("释放锁异常", e);
                        }
                    } else if(remaining == 0) {
                        try {
                            zooKeeper.delete(createPath, -1);
                        } catch (InterruptedException | KeeperException e) {
                            throw new CustomException("释放锁异常", e);
                        }
                        currentThreadId.remove();
                        lockCountMap.remove(threadId);
                    } else {
                        throw new CustomException("无效的解锁操作");
                    }
                }
            }, "");
        }
        throw new CustomException("无效的解锁操作");
    }
}
