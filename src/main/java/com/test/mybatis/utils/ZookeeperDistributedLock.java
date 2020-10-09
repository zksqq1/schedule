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
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper实现的分布式锁
 */
@ConditionalOnBean(ZooKeeper.class)
public class ZookeeperDistributedLock extends DistributedLock {
    private static ThreadLocal<String> currentThreadId = new ThreadLocal<>();
    @Autowired
    private ZooKeeper zooKeeper;

    @Override
    public boolean tryLock(String key, long millSeconds) {
        CountDownLatch latch = new CountDownLatch(1);
        String createPath = "/" + key;
        String current = currentThreadId.get();
        zooKeeper.getData(createPath, false, (rc, path, ctx, data, stat) -> {
            if (data != null) {
                String dataStr = new String(data);
                String[] split = dataStr.split("-");
                if (Objects.equals(current, split[0])) {
                    try {
                        zooKeeper.setData(createPath, (current + (Integer.parseInt(split[1]) + 1)).getBytes(StandardCharsets.UTF_8.name()), -1);
                    } catch (UnsupportedEncodingException | KeeperException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new CustomException("获取分布式锁失败");
                }
            } else {
                String value = UUID.randomUUID().toString().replace("-", "");
                try {
                    zooKeeper.create(createPath, (value + "-1").getBytes(StandardCharsets.UTF_8.name()), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
                            (rc1, path1, ctx1, name, stat1) -> {
                                if (path1 != null) {
                                    latch.countDown();
                                } else {
                                    throw new CustomException("获取分布式锁失败");
                                }
                            }, "");
                    currentThreadId.set(value);
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
    public boolean releaseLock(String key) {
        String createPath = "/" + key;
        String current = currentThreadId.get();
        zooKeeper.getData(createPath, false, (rc, path, ctx, data, stat) -> {
            String[] split = new String(data).split("-");
            if (Objects.equals(split[0], current)) {
                int times = Integer.parseInt(split[1]);
                if (times > 1) {
                    try {
                        zooKeeper.setData(createPath, (split[0] + (times - 1)).getBytes(StandardCharsets.UTF_8.name()), -1, (rc1, path1, ctx1, stat1) -> {
                            if (path1 == null) {
                                throw new CustomException("获取分布式锁失败");
                            }
                        }, "");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        zooKeeper.delete(createPath, -1);
                    } catch (InterruptedException | KeeperException e) {
                        e.printStackTrace();
                    }
                    currentThreadId.remove();
                }
            }
        }, "");
        return true;
    }
}
