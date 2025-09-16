package org.labs.core.stock;

final class AtomicFoodStock implements FoodStock {
    public AtomicFoodStock(long initialPortions) {
        // TODO: init atomic counter
    }

    @Override
    public boolean tryTakeOne() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long remaining() {
        throw new UnsupportedOperationException("Not implemented");
    }
}