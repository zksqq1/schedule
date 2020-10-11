package com.test.mybatis;

import com.test.mybatis.utils.RedisDistributedLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

/**
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MybatisApplication.class)
public class DistributedLockTest {
    @Autowired
    RedisDistributedLock redisLock;

    @Test
    public void redisTest() {
        String holderId = UUID.randomUUID().toString();
        redisLock.tryLock("lock1", 30000, holderId);
        redisLock.tryLock("lock1", 30000, holderId);
        redisLock.tryLock("lock2", 30000, holderId);
        redisLock.releaseLock("lock2", holderId);
        redisLock.tryLock("lock2", 30000, holderId);
        redisLock.releaseLock("lock1", holderId);
        redisLock.releaseLock("lock2", holderId);
        redisLock.releaseLock("lock1", holderId);
    }
}
