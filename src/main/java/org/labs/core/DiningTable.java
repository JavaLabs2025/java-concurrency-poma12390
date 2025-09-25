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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class DiningTable {
    private final SimulationConfig cfg;
    private final List<Spoon> spoons;
    private final List<Programmer> programmers;
    private final List<Waiter> waiters;
    private final BlockingQueue<RefillRequest> refillQueue;
    private final FoodStock stock;
    private final DeadlockAvoidanceStrategy deadlockStrategy;
    private final FairnessStrategy fairness;
    private final SimulationStats stats;

    private final ExecutorService waiterPool;
    private final ExecutorService programmerPool;

    private final List<Future<?>> waiterFutures = new ArrayList<>();
    private final List<Future<?>> programmerFutures = new ArrayList<>();

    private volatile boolean started = false;
    private volatile boolean stopped = false;

    public DiningTable(
            SimulationConfig cfg,
            List<Spoon> spoons,
            List<Programmer> programmers,
            List<Waiter> waiters,
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
        this.refillQueue = Objects.requireNonNull(refillQueue, "refillQueue");
        this.stock = Objects.requireNonNull(stock, "stock");
        this.deadlockStrategy = Objects.requireNonNull(deadlockStrategy, "deadlockStrategy");
        this.fairness = Objects.requireNonNull(fairness, "fairness");
        this.stats = Objects.requireNonNull(stats, "stats");

        if (this.spoons.size() < this.programmers.size()) {
            throw new IllegalArgumentException("spoons count must be >= programmers count");
        }
        if (cfg.programmers() != this.programmers.size()) {
            throw new IllegalArgumentException("cfg.programmers must equal provided programmers size");
        }
        ThreadFactory waiterFactory = r -> {
            Thread t = new Thread(r);
            t.setName("waiter-" + t.getName());
            return t;
        };
        ThreadFactory programmerFactory = r -> {
            Thread t = new Thread(r);
            t.setName("programmer-" + t.getName());
            return t;
        };

        this.waiterPool = Executors.newFixedThreadPool(this.waiters.size(), waiterFactory);

        this.programmerPool = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("programmer-", 0).factory());
    }

    public synchronized void start() {
        ensureNotStopped();
        if (started) throw new IllegalStateException("DiningTable already started");
        started = true;

        for (Waiter w : waiters) {
            waiterFutures.add(waiterPool.submit(w));
        }
        for (Programmer p : programmers) {
            programmerFutures.add(programmerPool.submit(p));
        }
    }

    public void awaitCompletion() throws InterruptedException {
        ensureStarted();

        for (Future<?> f : programmerFutures) {
            try {
                f.get();
            } catch (ExecutionException e) {
                System.err.println("[DiningTable] programmer task failed: " + e.getCause());
            }
        }

        shutdownWaitersGracefully();

        waiterPool.shutdownNow();
        waiterPool.awaitTermination(5, TimeUnit.SECONDS);

        programmerPool.shutdown();
        programmerPool.awaitTermination(5, TimeUnit.SECONDS);

        stopped = true;
    }

    public synchronized void shutdownNow() {
        if (stopped) return;

        for (Waiter w : waiters) {
            try {
                w.shutdown();
            } catch (Throwable ignored) {
            }
        }
        waiterPool.shutdownNow();
        programmerPool.shutdownNow();

        refillQueue.clear();

        stopped = true;
    }

    private void shutdownWaitersGracefully() {
        for (Waiter w : waiters) {
            try {
                w.shutdown();
            } catch (Throwable ignored) {
            }
        }
    }

    private void ensureStarted() {
        if (!started) throw new IllegalStateException("DiningTable not started");
    }

    private void ensureNotStopped() {
        if (stopped) throw new IllegalStateException("DiningTable already stopped");
    }


    public List<Future<?>> programmerTasks() {
        return List.copyOf(programmerFutures);
    }

    public List<Future<?>> waiterTasks() {
        return List.copyOf(waiterFutures);
    }

    public ExecutorService programmerExecutor() {
        return programmerPool;
    }

    public ExecutorService waiterExecutor() {
        return waiterPool;
    }
}
