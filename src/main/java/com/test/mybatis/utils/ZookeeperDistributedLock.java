package com.test.mybatis.utils;


import com.test.mybatis.exception.CustomException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
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
    private static ThreadLocal<String> currentThreadId = new ThreadLocal<>();
    private static ConcurrentHashMap<String, Integer> lockCountMap = new ConcurrentHashMap<>();

    @Autowired
    private ZooKeeper zooKeeper;

    @Override
    public boolean tryLock(String key, long millSeconds) throws CustomException {
        CountDownLatch latch = new CountDownLatch(1);
        String createPath = "/" + key;
        String threadId = currentThreadId.get() == null ? UUID.randomUUID().toString().replace("-", "") : currentThreadId.get();
        zooKeeper.getData(createPath, false, (rc, path, ctx, data, stat) -> {
            if (data != null) {
                String existsThreadId = new String(data);
                if (Objects.equals(threadId, existsThreadId)) {
                    lockCountMap.computeIfPresent(threadId, (k ,v) -> lockCountMap.get(k) + 1);
                    latch.countDown();
                } else {
                    throw new CustomException("锁已被占用，获取失败");
                }
            } else {
                try {
                    zooKeeper.create(createPath, threadId.getBytes(StandardCharsets.UTF_8.name()), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
                            (rc1, path1, ctx1, name, stat1) -> {
                                if (path1 != null) {
                                    latch.countDown();
                                } else {
                                    throw new CustomException("锁已被占用，获取失败");
                                }
                            }, "");
                    currentThreadId.set(threadId);
                    lockCountMap.put(threadId, 1);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }, "");
        try {
            latch.await();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
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
                                if (path1 == null) {
                                    throw new CustomException("释放分布式锁失败");
                                }
                            }, "");
                            lockCountMap.put(threadId, remaining);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else if(remaining == 0) {
                        try {
                            zooKeeper.delete(createPath, -1);
                        } catch (InterruptedException | KeeperException e) {
                            e.printStackTrace();
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
