package org.labs.core.actors;

import org.junit.jupiter.api.Test;
import org.labs.config.SimulationConfig;
import org.labs.core.resources.LockingSpoon;
import org.labs.core.resources.Spoon;
import org.labs.core.stock.AtomicFoodStock;
import org.labs.core.stock.FoodStock;
import org.labs.metrics.SimulationStats;
import org.labs.model.RefillRequest;
import org.labs.strategy.deadlock.NMinusOneDeadlockStrategy;
import org.labs.strategy.fairness.FairnessStrategy;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProgrammerTest {

    static final class NoopFairness implements FairnessStrategy {
        @Override public boolean tryEnterEat(int programmerId, long alreadyEaten) { return true; }
        @Override public void onStartEat(int programmerId) { }
        @Override public void onFinishEat(int programmerId) { }
    }

    @Test
    void eatsExactNumberOfPortionsAndStops() throws Exception {
        SimulationConfig cfg = SimulationConfig.builder()
                .programmers(2).waiters(1).totalPortions(3)
                .thinkRange(Duration.ofMillis(1), Duration.ofMillis(3))
                .eatRange(Duration.ofMillis(1), Duration.ofMillis(3))
                .spoonAcquireTimeout(Duration.ofMillis(20))
                .fairShareRequired(false)
                .build();

        FoodStock stock = new AtomicFoodStock(cfg.totalPortions());
        BlockingQueue<RefillRequest> queue = new LinkedBlockingQueue<>();
        SimulationStats stats = new SimulationStats();

        Thread waiter = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    RefillRequest r = queue.poll(200, TimeUnit.MILLISECONDS);
                    if (r == null) continue;
                    boolean ok = stock.tryTakeOne();
                    r.tryComplete(ok);
                    if (!ok) break;
                }
            } catch (InterruptedException ignored) { }
        }, "test-waiter");
        waiter.start();

        Spoon left = new LockingSpoon(0);
        Spoon right = new LockingSpoon(1);

        Programmer p = new Programmer(
                0, left, right, cfg,
                new NMinusOneDeadlockStrategy(2),
                new NoopFairness(),
                queue, stock, stats
        );

        Thread pt = new Thread(p, "programmer-0");
        pt.start();
        pt.join(2000);

        waiter.interrupt();
        waiter.join(1000);

        assertFalse(pt.isAlive(), "Programmer should finish");
        assertEquals(3, p.portionsEaten());
        assertEquals(3, stats.totalConsumed());
    }
}
