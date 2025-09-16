package org.labs.strategy.deadlock;

final class NMinusOneDeadlockStrategy implements DeadlockAvoidanceStrategy {
    public NMinusOneDeadlockStrategy(int participants) {
        // TODO: инициализация семафора на (participants - 1)
    }

    @Override
    public void beforeAcquire(int programmerId) throws InterruptedException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void afterRelease(int programmerId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}