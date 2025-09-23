package org.labs.core.actors;

import org.labs.config.SimulationConfig;
import org.labs.core.resources.Spoon;
import org.labs.core.stock.FoodStock;
import org.labs.metrics.SimulationStats;
import org.labs.model.ProgrammerState;
import org.labs.model.RefillRequest;
import org.labs.strategy.deadlock.DeadlockAvoidanceStrategy;
import org.labs.strategy.fairness.FairnessStrategy;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class Programmer implements Runnable {
    private final int id;
    private final Spoon left;
    private final Spoon right;
    private final SimulationConfig cfg;
    private final DeadlockAvoidanceStrategy deadlockStrategy;
    private final FairnessStrategy fairness;
    private final BlockingQueue<RefillRequest> refillQueue;
    private final FoodStock stock;
    private final SimulationStats stats; // может быть null, если метрики не нужны

    private volatile ProgrammerState state = ProgrammerState.THINKING;
    private long portionsEaten = 0;
    private boolean hasPortion = false; // локальная тарелка: есть ли непросаженная порция

    public Programmer(
            int id,
            Spoon left,
            Spoon right,
            SimulationConfig cfg,
            DeadlockAvoidanceStrategy deadlockStrategy,
            FairnessStrategy fairness,
            BlockingQueue<RefillRequest> refillQueue,
            FoodStock stock,
            SimulationStats stats
    ) {
        if (id < 0) throw new IllegalArgumentException("id must be >= 0");
        this.id = id;
        this.left = Objects.requireNonNull(left, "left");
        this.right = Objects.requireNonNull(right, "right");
        this.cfg = Objects.requireNonNull(cfg, "cfg");
        this.deadlockStrategy = Objects.requireNonNull(deadlockStrategy, "deadlockStrategy");
        this.fairness = Objects.requireNonNull(fairness, "fairness");
        this.refillQueue = Objects.requireNonNull(refillQueue, "refillQueue");
        this.stock = Objects.requireNonNull(stock, "stock");
        this.stats = stats; // допускаем null
    }

    public int id() { return id; }

    public long portionsEaten() { return portionsEaten; }

    @Override
    public void run() {
        try {
            mainLoop();
        } finally {
            updateState(ProgrammerState.DONE);
        }
    }

    private void mainLoop() {
        final long acquireTimeoutMs = Math.max(0, cfg.spoonAcquireTimeout().toMillis());

        while (!Thread.currentThread().isInterrupted()) {
            try {
                think();

                if (!hasPortion) {
                    boolean refilled = requestRefillAndAwait();
                    if (!refilled) {
                        if (!hasPortion) {
                            break;
                        }
                    }
                }

                boolean gateEntered = false;
                try {
                    deadlockStrategy.beforeAcquire(id);
                    gateEntered = true;

                    Spoon first = left.id() <= right.id() ? left : right;
                    Spoon second = left.id() <= right.id() ? right : left;

                    updateState(ProgrammerState.WAITING_SPOONS);
                    if (!first.tryAcquire(acquireTimeoutMs, TimeUnit.MILLISECONDS)) {
                        continue;
                    }

                    boolean secondAcquired = false;
                    try {
                        if (!second.tryAcquire(acquireTimeoutMs, TimeUnit.MILLISECONDS)) {
                            continue;
                        }
                        secondAcquired = true;

                        if (!fairness.tryEnterEat(id, portionsEaten)) {
                            continue;
                        }

                        fairness.onStartEat(id);
                        updateState(ProgrammerState.EATING);
                        eat();
                        fairness.onFinishEat(id);

                    } finally {
                        if (secondAcquired) {
                            try { second.release(); } catch (Throwable ignored) {}
                        }
                        try { first.release(); } catch (Throwable ignored) {}
                    }

                } finally {
                    if (gateEntered) {
                        try { deadlockStrategy.afterRelease(id); } catch (Throwable ignored) {}
                    }
                }

                if (stock.isDepleted() && !hasPortion) {
                    break;
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    void think() throws InterruptedException {
        updateState(ProgrammerState.THINKING);
        randomSleepBetween(cfg.minThink(), cfg.maxThink());
    }

    void eat() throws InterruptedException {
        if (!hasPortion) {
            return;
        }
        randomSleepBetween(cfg.minEat(), cfg.maxEat());
        hasPortion = false;
        portionsEaten++;
        if (stats != null) {
            stats.onPortionConsumed(id);
        }
    }

    boolean requestRefillAndAwait() throws InterruptedException {
        updateState(ProgrammerState.REQUESTING_REFILL);
        RefillRequest req = new RefillRequest(id);
        updateState(ProgrammerState.WAITING_REFILL);

        refillQueue.put(req);
        boolean granted;
        granted = req.awaitResult();

        if (granted) {
            hasPortion = true;
            return true;
        } else {
            hasPortion = false;
            return false;
        }
    }

    private static void randomSleepBetween(Duration min, Duration max) throws InterruptedException {
        long minMs = Math.max(0, min.toMillis());
        long maxMs = Math.max(minMs, max.toMillis());
        long span = maxMs - minMs;
        long sleepMs = minMs + (span == 0 ? 0 : ThreadLocalRandom.current().nextLong(span + 1));
        if (sleepMs > 0) {
            Thread.sleep(sleepMs);
        } else {
            Thread.onSpinWait();
        }
    }

    private void updateState(ProgrammerState newState) {
        this.state = newState;
        if (stats != null) {
            stats.onProgrammerStateChange(id, newState);
        }
    }

    @Override
    public String toString() {
        return "Programmer{" +
                "id=" + id +
                ", state=" + state +
                ", portionsEaten=" + portionsEaten +
                ", hasPortion=" + hasPortion +
                '}';
    }
}