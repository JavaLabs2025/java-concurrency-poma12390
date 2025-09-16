package org.labs.strategy.fairness;

import java.util.Map;

public interface FairnessStrategy {
    /** Разрешить ли программисту начинать есть прямо сейчас. Может блокировать или возвращать false. */
    boolean tryEnterEat(int programmerId, long alreadyEaten);

    /** Сигнал о начале еды. */
    void onStartEat(int programmerId);

    /** Сигнал об окончании еды. */
    void onFinishEat(int programmerId);

    /** Текущая статистика порций по программистам. */
    Map<Integer, Long> portionsByProgrammer();
}