package com.spartaecommerce.common.infrastructure.lock.manager;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedissonLockManager implements DistributedLockManager {

    private static final String LOCK_PREFIX = "lock:";

    private final RedissonClient redissonClient;

    private final ThreadLocal<RLock> currentLock = new ThreadLocal<>();

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime) {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            if (acquired) {
                currentLock.set(lock);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        RLock lock = currentLock.get();
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            currentLock.remove();
        }
    }
}
