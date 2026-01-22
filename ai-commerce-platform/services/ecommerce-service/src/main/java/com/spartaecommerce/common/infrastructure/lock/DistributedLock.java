package com.spartaecommerce.common.infrastructure.lock;

public @interface DistributedLock {

    String key();

    long waitTime() default 5000L;

    long leaseTime() default 3000L;

    String errorMessage() default "Failed to acquire lock";
}
