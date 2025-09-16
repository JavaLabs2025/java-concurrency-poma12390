package org.labs.core.resources;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

final class LockingSpoon implements Spoon {
    private final int id;

    public LockingSpoon(int id) {
        this.id = id;
    }

    @Override public int id() { return id; }

    @Override
    public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void release() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Lock> underlyingLock() {
        return Optional.empty();
    }
}