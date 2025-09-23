package org.labs.strategy.fairness;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public final class EqualShareFairness implements FairnessStrategy {

    private final int slack;

    private final ConcurrentMap<Integer, AtomicLong> finishedPerProgrammer = new ConcurrentHashMap<>();

    /**
     * @param slack допустимый отступ от минимума (>= 0).
     *              0 — строгое равенство (начать есть можно только тем, кто в минимуме).
     *              1 — по умолчанию, мягкая справедливость.
     */
    public EqualShareFairness(int slack) {
        if (slack < 0) {
            throw new IllegalArgumentException("slack must be >= 0");
        }
        this.slack = slack;
    }

    @Override
    public boolean tryEnterEat(int programmerId, long alreadyEaten) {
        // Убедимся, что участник присутствует в карте (значение – завершённые порции).
        finishedPerProgrammer.computeIfAbsent(programmerId, k -> new AtomicLong(0L));

        long minFinished = currentMinFinished();
        return alreadyEaten <= minFinished + slack;
    }

    @Override
    public void onStartEat(int programmerId) {
        // no-op
    }

    @Override
    public void onFinishEat(int programmerId) {
        finishedPerProgrammer
                .computeIfAbsent(programmerId, k -> new AtomicLong(0L))
                .incrementAndGet();
    }

    private long currentMinFinished() {
        long min = Long.MAX_VALUE;
        boolean empty = true;

        for (AtomicLong v : finishedPerProgrammer.values()) {
            long val = v.longValue();
            if (val < min) {
                min = val;
            }
            empty = false;
        }
        return empty ? 0L : min;
    }

}