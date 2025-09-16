package org.labs.strategy.deadlock;

public interface DeadlockAvoidanceStrategy {
    /** Вызвать перед захватом ложек. Можно блокировать, пока нельзя есть. */
    void beforeAcquire(int programmerId) throws InterruptedException;

    /** Вызвать после освобождения ложек. */
    void afterRelease(int programmerId);
}