package org.labs.strategy.deadlock;

import java.util.concurrent.Semaphore;

public final class NMinusOneDeadlockStrategy implements DeadlockAvoidanceStrategy {
    private final Semaphore gate;

    public NMinusOneDeadlockStrategy(int participants) {
        this(participants, true);
    }

    public NMinusOneDeadlockStrategy(int participants, boolean fair) {
        if (participants < 2) {
            throw new IllegalArgumentException("participants must be >= 2");
        }
        this.gate = new Semaphore(participants - 1, fair);
    }

    @Override
    public void beforeAcquire(int programmerId) throws InterruptedException {
        gate.acquire();
    }

    @Override
    public void afterRelease(int programmerId) {
        gate.release();
    }

    public int availableSlots() {
        return gate.availablePermits();
    }
}
