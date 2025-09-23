package org.labs.strategy.deadlock;

public interface DeadlockAvoidanceStrategy {
    void beforeAcquire(int programmerId) throws InterruptedException;

    void afterRelease(int programmerId);
}