package org.labs.strategy.deadlock;

final class OrderedSpoonsDeadlockStrategy implements DeadlockAvoidanceStrategy {
    @Override
    public void beforeAcquire(int programmerId) throws InterruptedException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void afterRelease(int programmerId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
