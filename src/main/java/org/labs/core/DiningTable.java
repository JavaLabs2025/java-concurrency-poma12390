package org.labs.core;

import org.labs.config.SimulationConfig;
import org.labs.core.actors.Programmer;
import org.labs.core.resources.Spoon;
import org.labs.core.stock.FoodStock;
import org.labs.metrics.SimulationStats;
import org.labs.model.RefillRequest;
import org.labs.strategy.deadlock.DeadlockAvoidanceStrategy;
import org.labs.strategy.fairness.FairnessStrategy;
import org.labs.waiter.Waiter;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public final class DiningTable {
    private final SimulationConfig cfg;
    private final List<Spoon> spoons;
    private final List<Programmer> programmers;
    private final List<Thread> waiterThreads;
    private final List<Waiter> waiters;
    private final BlockingQueue<RefillRequest> refillQueue;
    private final FoodStock stock;
    private final DeadlockAvoidanceStrategy deadlockStrategy;
    private final FairnessStrategy fairness;
    private final SimulationStats stats;

    public DiningTable(
            SimulationConfig cfg,
            List<Spoon> spoons,
            List<Programmer> programmers,
            List<Waiter> waiters,
            List<Thread> waiterThreads,
            BlockingQueue<RefillRequest> refillQueue,
            FoodStock stock,
            DeadlockAvoidanceStrategy deadlockStrategy,
            FairnessStrategy fairness,
            SimulationStats stats
    ) {
        this.cfg = cfg;
        this.spoons = spoons;
        this.programmers = programmers;
        this.waiters = waiters;
        this.waiterThreads = waiterThreads;
        this.refillQueue = refillQueue;
        this.stock = stock;
        this.deadlockStrategy = deadlockStrategy;
        this.fairness = fairness;
        this.stats = stats;
    }

    /** Запуск всех потоков. */
    public void start() {
        // TODO: старт официантов и программистов
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Дождаться завершения (нет еды или достигнуты критерии остановки). */
    public void awaitCompletion() throws InterruptedException {
        // TODO: join потоков
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Аварийная остановка. */
    public void shutdownNow() {
        // TODO: прервать все потоки, закрыть ресурсы
        throw new UnsupportedOperationException("Not implemented");
    }

    public SimulationStats stats() { return stats; }
}