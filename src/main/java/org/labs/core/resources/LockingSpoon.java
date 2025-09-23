package org.labs.core.resources;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public final class LockingSpoon implements Spoon {
    private final int id;
    private final ReentrantLock lock;

    public LockingSpoon(int id) {
        this(id, false);
    }

    public LockingSpoon(int id, boolean fair) {
        if (id < 0) throw new IllegalArgumentException("id must be >= 0");
        this.id = id;
        this.lock = new ReentrantLock(fair);
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        if (timeout < 0) throw new IllegalArgumentException("timeout must be >= 0");
        Objects.requireNonNull(unit, "unit");
        return lock.tryLock(timeout, unit);
    }

    @Override
    public void release() {
        if (!lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Thread " + Thread.currentThread().getName()
                    + " attempted to release spoon #" + id + " without owning its lock");
        }
        lock.unlock();
    }
}