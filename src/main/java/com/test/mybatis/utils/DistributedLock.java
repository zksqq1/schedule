package com.test.mybatis.utils;

import com.test.mybatis.exception.CustomException;

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
    public abstract boolean tryLock(String key, long millSeconds) throws CustomException;

    /**
     * 释放锁
     * @param key 锁信息
     * @return
     */
    public abstract void releaseLock(String key) throws CustomException;
}