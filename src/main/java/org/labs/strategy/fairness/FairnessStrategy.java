package org.labs.strategy.fairness;

public interface FairnessStrategy {
    /** Может ли программист начать есть */
    boolean tryEnterEat(int programmerId, long alreadyEaten);

    /** Сигнал о начале еды */
    void onStartEat(int programmerId);

    /** Сигнал об окончании еды */
    void onFinishEat(int programmerId);
}