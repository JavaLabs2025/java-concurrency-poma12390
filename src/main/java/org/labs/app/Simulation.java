package org.labs.app;

import org.labs.config.SimulationConfig;
import org.labs.core.DiningTable;
import org.labs.core.actors.Programmer;
import org.labs.core.resources.LockingSpoon;
import org.labs.core.resources.Spoon;
import org.labs.core.stock.AtomicFoodStock;
import org.labs.core.stock.FoodStock;
import org.labs.metrics.SimulationStats;
import org.labs.model.RefillRequest;
import org.labs.strategy.deadlock.DeadlockAvoidanceStrategy;
import org.labs.strategy.deadlock.NMinusOneDeadlockStrategy;
import org.labs.strategy.deadlock.OrderedSpoonsDeadlockStrategy;
import org.labs.strategy.fairness.EqualShareFairness;
import org.labs.strategy.fairness.FairnessStrategy;
import org.labs.waiter.DefaultWaiter;
import org.labs.waiter.Waiter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class Simulation {

    private static final ProgrammaticOverrides OVERRIDES = new ProgrammaticOverrides(
            7,          // programmers (Integer)   e.g. 7
            null,          // waiters (Integer)       e.g. 2
            2000L,          // totalPortions (Long)    e.g. 1000
            null, null,    // thinkMinMs, thinkMaxMs (Long)
            null, null,    // eatMinMs,   eatMaxMs   (Long)
            null,          // acquireTimeoutMs        (Long)
            null           // fairShareRequired       (Boolean)
    );
    private static final int SLACK = 2;

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) ->
                System.err.println("[FATAL] " + t.getName() + " crashed: " + e));

        SimulationConfig cfg = buildConfigFromArgsOrOverrides(args);

        SimulationStats stats = new SimulationStats();
        FoodStock stock = new AtomicFoodStock(cfg.totalPortions());
        BlockingQueue<RefillRequest> refillQueue = new LinkedBlockingQueue<>();

        DeadlockAvoidanceStrategy deadlock = chooseDeadlockStrategy(cfg, args);
        FairnessStrategy fairness = chooseFairnessStrategy(cfg);

        int n = cfg.programmers();
        List<Spoon> spoons = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            spoons.add(new LockingSpoon(i));
        }

        List<Waiter> waiters = new ArrayList<>(cfg.waiters());
        List<Thread> waiterThreads = new ArrayList<>(cfg.waiters());
        for (int i = 0; i < cfg.waiters(); i++) {
            Waiter w = new DefaultWaiter(i, refillQueue, stock);
            Thread wt = new Thread(w, "waiter-" + i);
            waiters.add(w);
            waiterThreads.add(wt);
        }

        List<Programmer> programmers = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Spoon left = spoons.get(i);
            Spoon right = spoons.get((i + 1) % n);
            programmers.add(new Programmer(
                    i, left, right, cfg, deadlock, fairness, refillQueue, stock, stats
            ));
        }

        DiningTable table = new DiningTable(
                cfg, spoons, programmers, waiters, waiterThreads,
                refillQueue, stock, deadlock, fairness, stats
        );

        // таймер для принудительной остановки, во избежание вечной работы
        Long maxSeconds = findLongArg(args);
        Thread watchdog;
        if (maxSeconds != null && maxSeconds > 0) {
            long ms = maxSeconds * 1000L;
            watchdog = new Thread(() -> {
                try {
                    Thread.sleep(ms);
                    System.err.println("[WATCHDOG] Max run reached (" + maxSeconds + "s). Shutting down...");
                    table.shutdownNow();
                } catch (InterruptedException ignored) { }
            }, "watchdog");
            watchdog.setDaemon(true);
            watchdog.start();
        }

        long t0 = System.nanoTime();
        table.start();

        try {
            table.awaitCompletion();
        } catch (InterruptedException e) {
            System.err.println("[MAIN] Interrupted while awaiting completion, shutting down...");
            Thread.currentThread().interrupt();
            table.shutdownNow();
        }
        long t1 = System.nanoTime();

        printSummary(stats, t0, t1);
    }

    private static SimulationConfig buildConfigFromArgsOrOverrides(String[] args) {
        if (args != null && args.length > 0) {
            return buildConfigFromArgs(args);
        }

        SimulationConfig.Builder b = SimulationConfig.builder();

        if (Simulation.OVERRIDES.programmers != null) b.programmers(Simulation.OVERRIDES.programmers);
        if (Simulation.OVERRIDES.waiters != null) b.waiters(Simulation.OVERRIDES.waiters);
        if (Simulation.OVERRIDES.totalPortions != null) b.totalPortions(Simulation.OVERRIDES.totalPortions);

        if (Simulation.OVERRIDES.thinkMinMs != null || Simulation.OVERRIDES.thinkMaxMs != null) {
            long min = Simulation.OVERRIDES.thinkMinMs != null ? Simulation.OVERRIDES.thinkMinMs : b.build().minThink().toMillis();
            long max = Simulation.OVERRIDES.thinkMaxMs != null ? Simulation.OVERRIDES.thinkMaxMs : b.build().maxThink().toMillis();
            b.thinkRange(Duration.ofMillis(min), Duration.ofMillis(max));
        }

        if (Simulation.OVERRIDES.eatMinMs != null || Simulation.OVERRIDES.eatMaxMs != null) {
            long min = Simulation.OVERRIDES.eatMinMs != null ? Simulation.OVERRIDES.eatMinMs : b.build().minEat().toMillis();
            long max = Simulation.OVERRIDES.eatMaxMs != null ? Simulation.OVERRIDES.eatMaxMs : b.build().maxEat().toMillis();
            b.eatRange(Duration.ofMillis(min), Duration.ofMillis(max));
        }

        if (Simulation.OVERRIDES.acquireTimeoutMs != null) {
            b.spoonAcquireTimeout(Duration.ofMillis(Simulation.OVERRIDES.acquireTimeoutMs));
        }

        if (Simulation.OVERRIDES.fairShareRequired != null) {
            b.fairShareRequired(Simulation.OVERRIDES.fairShareRequired);
        }

        return b.build();
    }

    private static SimulationConfig buildConfigFromArgs(String[] args) {
        SimulationConfig.Builder b = SimulationConfig.builder();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-n":
                case "--programmers":
                    b.programmers(Integer.parseInt(next(args, ++i, "programmers")));
                    break;
                case "-w":
                case "--waiters":
                    b.waiters(Integer.parseInt(next(args, ++i, "waiters")));
                    break;
                case "-p":
                case "--portions":
                    b.totalPortions(Long.parseLong(next(args, ++i, "portions")));
                    break;
                case "--think": {
                    String[] mm = next(args, ++i, "think").split(",");
                    b.thinkRange(Duration.ofMillis(Long.parseLong(mm[0])), Duration.ofMillis(Long.parseLong(mm[1])));
                    break;
                }
                case "--eat": {
                    String[] mm = next(args, ++i, "eat").split(",");
                    b.eatRange(Duration.ofMillis(Long.parseLong(mm[0])), Duration.ofMillis(Long.parseLong(mm[1])));
                    break;
                }
                case "--acquire-timeout":
                    b.spoonAcquireTimeout(Duration.ofMillis(Long.parseLong(next(args, ++i, "acquire-timeout"))));
                    break;
                case "--no-fairness":
                    b.fairShareRequired(false);
                    break;
                default:
                    break;
            }
        }

        return b.build();
    }

    private static DeadlockAvoidanceStrategy chooseDeadlockStrategy(SimulationConfig cfg, String[] args) {
        String mode = null;
        for (int i = 0; i < args.length; i++) {
            if ("--strategy".equals(args[i]) && i + 1 < args.length) {
                mode = args[i + 1];
                break;
            }
        }
        if ("ordered".equalsIgnoreCase(mode)) {
            return new OrderedSpoonsDeadlockStrategy();
        }
        return new NMinusOneDeadlockStrategy(cfg.programmers());
    }

    private static FairnessStrategy chooseFairnessStrategy(SimulationConfig cfg) {
        if (!cfg.fairShareRequired()) return new NoopFairness();
        return new EqualShareFairness(SLACK);
    }

    private static String next(String[] a, int idx, String name) {
        if (idx >= a.length) throw new IllegalArgumentException("Missing value for " + name);
        return a[idx];
    }

    private static Long findLongArg(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--max-seconds".equals(args[i]) && i + 1 < args.length) {
                try { return Long.parseLong(args[i + 1]); }
                catch (NumberFormatException ignore) { return null; }
            }
        }
        return null;
    }

    private static void printSummary(SimulationStats stats, long t0, long t1) {
        double sec = (t1 - t0) / 1_000_000_000.0;
        System.out.println("\n=== Simulation Summary ===");
        System.out.printf("Elapsed: %.3f s%n", sec);
        System.out.println("Total consumed: " + stats.totalConsumed());
        System.out.println("State distribution: " + stats.stateDistribution());
        var perProg = stats.summaryByProgrammer();
        System.out.println("Per-programmer portions:");
        perProg.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .forEach(e -> System.out.printf("  #%d : %d%n", e.getKey(), e.getValue()));
    }


    private static final class NoopFairness implements FairnessStrategy {
        @Override public boolean tryEnterEat(int programmerId, long alreadyEaten) { return true; }
        @Override public void onStartEat(int programmerId) { }
        @Override public void onFinishEat(int programmerId) { }
    }

    private record ProgrammaticOverrides(
            Integer programmers,
            Integer waiters,
            Long totalPortions,
            Long thinkMinMs, Long thinkMaxMs,
            Long eatMinMs,   Long eatMaxMs,
            Long acquireTimeoutMs,
            Boolean fairShareRequired
    ) { }
}
