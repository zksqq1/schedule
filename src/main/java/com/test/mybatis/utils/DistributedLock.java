package com.test.mybatis.utils;

/**
 * redis 分布式锁
 */
public abstract class DistributedLock {
    /**
     * 获取分布式锁
     * @param key 锁信息
     * @param millSeconds 失效时间，毫秒值
     * @return
     */
    public abstract boolean tryLock(String key, long millSeconds);

    /**
     * 释放锁
     * @param key 锁信息
     * @return
     */
    public abstract boolean releaseLock(String key);
}