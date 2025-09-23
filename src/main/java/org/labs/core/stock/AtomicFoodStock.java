package org.labs.core.stock;

import java.util.concurrent.atomic.AtomicLong;

public final class AtomicFoodStock implements FoodStock {
    private final AtomicLong portions;

    public AtomicFoodStock(long initialPortions) {
        if (initialPortions < 0) {
            throw new IllegalArgumentException("initialPortions must be >= 0");
        }
        this.portions = new AtomicLong(initialPortions);
    }

    @Override
    public boolean tryTakeOne() {
        while (true) {
            long current = portions.get();
            if (current <= 0) {
                return false;
            }
            if (portions.compareAndSet(current, current - 1)) {
                return true;
            }
        }
    }

    @Override
    public long remaining() {
        return portions.get();
    }

    @Override
    public String toString() {
        return "AtomicFoodStock{remaining=" + portions.get() + '}';
    }
}