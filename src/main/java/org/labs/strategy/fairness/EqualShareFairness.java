package org.labs.strategy.fairness;

import java.util.Map;

final class EqualShareFairness implements FairnessStrategy {
    @Override
    public boolean tryEnterEat(int programmerId, long alreadyEaten) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void onStartEat(int programmerId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void onFinishEat(int programmerId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<Integer, Long> portionsByProgrammer() {
        throw new UnsupportedOperationException("Not implemented");
    }
}