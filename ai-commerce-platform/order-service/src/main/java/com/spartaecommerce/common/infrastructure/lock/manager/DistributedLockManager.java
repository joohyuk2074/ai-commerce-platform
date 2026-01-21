package com.spartaecommerce.common.infrastructure.lock.manager;

public interface DistributedLockManager {

    boolean tryLock(String key, long waitTime, long leaseTime);

    void unlock(String key);
}
