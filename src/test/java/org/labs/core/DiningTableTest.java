package org.labs.core;

import org.junit.jupiter.api.Test;
import org.labs.config.SimulationConfig;
import org.labs.core.actors.Programmer;
import org.labs.core.resources.LockingSpoon;
import org.labs.core.resources.Spoon;
import org.labs.core.stock.AtomicFoodStock;
import org.labs.core.stock.FoodStock;
import org.labs.metrics.SimulationStats;
import org.labs.model.RefillRequest;
import org.labs.strategy.deadlock.DeadlockAvoidanceStrategy;
import org.labs.strategy.deadlock.NMinusOneDeadlockStrategy;
import org.labs.strategy.fairness.EqualShareFairness;
import org.labs.strategy.fairness.FairnessStrategy;
import org.labs.waiter.DefaultWaiter;
import org.labs.waiter.Waiter;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class DiningTableIntegrationTest {

    @Test
    void smallSystemFinishesAndConsumesAll() throws Exception {
        int N = 3;
        long portions = 30;

        SimulationConfig cfg = SimulationConfig.builder()
                .programmers(N).waiters(1).totalPortions(portions)
                .thinkRange(Duration.ofMillis(1), Duration.ofMillis(3))
                .eatRange(Duration.ofMillis(1), Duration.ofMillis(3))
                .spoonAcquireTimeout(Duration.ofMillis(30))
                .fairShareRequired(true)
                .build();

        SimulationStats stats = new SimulationStats();
        FoodStock stock = new AtomicFoodStock(cfg.totalPortions());
        BlockingQueue<RefillRequest> q = new LinkedBlockingQueue<>();

        DeadlockAvoidanceStrategy deadlock = new NMinusOneDeadlockStrategy(cfg.programmers());
        FairnessStrategy fairness = new EqualShareFairness(1);

        List<Spoon> spoons = new ArrayList<>(N);
        for (int i = 0; i < N; i++) spoons.add(new LockingSpoon(i));

        List<Waiter> waiters = List.of(new DefaultWaiter(0, q, stock));

        List<Programmer> programmers = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            Spoon left = spoons.get(i);
            Spoon right = spoons.get((i + 1) % N);
            programmers.add(new Programmer(i, left, right, cfg, deadlock, fairness, q, stock, stats));
        }

        DiningTable table = new DiningTable(cfg, spoons, programmers, waiters, q, stock, deadlock, fairness, stats);

        table.start();
        table.awaitCompletion();

        assertEquals(portions, stats.totalConsumed(), "All portions must be consumed");
        long sum = stats.summaryByProgrammer().values().stream().mapToLong(Long::longValue).sum();
        assertEquals(portions, sum, "Sum across programmers must equal total portions");

        var vals = new ArrayList<>(stats.summaryByProgrammer().values());
        long max = Collections.max(vals);
        long min = Collections.min(vals);
        assertTrue(max - min <= 2, "Fairness should keep difference small");
    }

    @Test
    void shutdownNowInterruptsAndClears() throws Exception {
        int N = 3;
        SimulationConfig cfg = SimulationConfig.builder()
                .programmers(N).waiters(1).totalPortions(10_000) // специально много
                .thinkRange(Duration.ofMillis(2), Duration.ofMillis(5))
                .eatRange(Duration.ofMillis(2), Duration.ofMillis(5))
                .spoonAcquireTimeout(Duration.ofMillis(50))
                .build();

        SimulationStats stats = new SimulationStats();
        FoodStock stock = new AtomicFoodStock(cfg.totalPortions());
        BlockingQueue<RefillRequest> q = new LinkedBlockingQueue<>();

        DeadlockAvoidanceStrategy deadlock = new NMinusOneDeadlockStrategy(cfg.programmers());
        FairnessStrategy fairness = new EqualShareFairness(1);

        List<Spoon> spoons = new ArrayList<>(N);
        for (int i = 0; i < N; i++) spoons.add(new LockingSpoon(i));

        List<Waiter> waiters = List.of(new DefaultWaiter(0, q, stock));

        List<Programmer> programmers = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            programmers.add(new Programmer(i, spoons.get(i), spoons.get((i + 1) % N), cfg, deadlock, fairness, q, stock, stats));
        }

        DiningTable table = new DiningTable(cfg, spoons, programmers, waiters, q, stock, deadlock, fairness, stats);
        table.start();

        // Немного поработаем
        try { Thread.sleep(50); } catch (InterruptedException ignored) { }

        // Аварийно остановим
        table.shutdownNow();

        // Ждём завершение пулов
        assertTrue(table.programmerExecutor().awaitTermination(2, TimeUnit.SECONDS), "Programmer pool did not terminate");
        assertTrue(table.waiterExecutor().awaitTermination(2, TimeUnit.SECONDS), "Waiter pool did not terminate");

        // Все задачи должны быть завершены/отменены
        table.programmerTasks().forEach(f -> assertTrue(f.isDone() || f.isCancelled()));
        table.waiterTasks().forEach(f -> assertTrue(f.isDone() || f.isCancelled()));
    }
}