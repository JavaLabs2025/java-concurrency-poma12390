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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    private final List<Thread> programmerThreads = new ArrayList<>();

    private volatile boolean started = false;
    private volatile boolean stopped = false;

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
        this.cfg = Objects.requireNonNull(cfg, "cfg");
        this.spoons = List.copyOf(Objects.requireNonNull(spoons, "spoons"));
        this.programmers = List.copyOf(Objects.requireNonNull(programmers, "programmers"));
        this.waiters = List.copyOf(Objects.requireNonNull(waiters, "waiters"));
        this.waiterThreads = new ArrayList<>(Objects.requireNonNull(waiterThreads, "waiterThreads"));
        this.refillQueue = Objects.requireNonNull(refillQueue, "refillQueue");
        this.stock = Objects.requireNonNull(stock, "stock");
        this.deadlockStrategy = Objects.requireNonNull(deadlockStrategy, "deadlockStrategy");
        this.fairness = Objects.requireNonNull(fairness, "fairness");
        this.stats = Objects.requireNonNull(stats, "stats");

        if (this.waiters.size() != this.waiterThreads.size()) {
            throw new IllegalArgumentException("waiters and waiterThreads sizes must match");
        }
        if (this.spoons.size() < this.programmers.size()) {
            throw new IllegalArgumentException("spoons count must be >= programmers count");
        }
        if (cfg.programmers() != this.programmers.size()) {
            throw new IllegalArgumentException("cfg.programmers must equal provided programmers size");
        }
    }

    public synchronized void start() {
        ensureNotStopped();
        if (started) {
            throw new IllegalStateException("DiningTable already started");
        }
        started = true;

        for (int i = 0; i < waiterThreads.size(); i++) {
            Thread t = waiterThreads.get(i);
            if (!t.isAlive()) {
                String name = "waiter-" + i;
                if (t.getName() == null || t.getName().isBlank()) {
                    t.setName(name);
                }
                t.start();
            }
        }

        for (Programmer p : programmers) {
            Thread t = new Thread(p, "programmer-" + p.id());
            programmerThreads.add(t);
            t.start();
        }
    }

    public void awaitCompletion() throws InterruptedException {
        ensureStarted();

        for (Thread t : programmerThreads) {
            t.join();
        }

        shutdownWaitersGracefully();

        for (Thread t : waiterThreads) {
            t.join();
        }

        stopped = true;
    }

    public synchronized void shutdownNow() {
        if (stopped) return;
        for (Waiter w : waiters) {
            try {
                w.shutdown();
            } catch (Throwable ignored) {}
        }
        for (Thread t : programmerThreads) {
            t.interrupt();
        }
        for (Thread t : waiterThreads) {
            t.interrupt();
        }
        refillQueue.clear();
        stopped = true;
    }

    private void shutdownWaitersGracefully() {
        for (Waiter w : waiters) {
            try {
                w.shutdown();
            } catch (Throwable ignored) {}
        }
        for (Thread t : waiterThreads) {
            t.interrupt();
        }
    }

    private void ensureStarted() {
        if (!started) {
            throw new IllegalStateException("DiningTable not started");
        }
    }

    private void ensureNotStopped() {
        if (stopped) {
            throw new IllegalStateException("DiningTable already stopped");
        }
    }

    public List<Thread> programmerThreads() { return Collections.unmodifiableList(programmerThreads); }
    public List<Thread> waiterThreads() { return Collections.unmodifiableList(waiterThreads); }

}