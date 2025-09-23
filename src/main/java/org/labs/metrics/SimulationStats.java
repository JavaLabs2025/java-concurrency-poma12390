package org.labs.metrics;

import org.labs.model.ProgrammerState;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

public final class SimulationStats {

    private final ConcurrentMap<Integer, ProgrammerState> states = new ConcurrentHashMap<>();
    private final EnumMap<ProgrammerState, LongAdder> stateDistribution =
            new EnumMap<>(ProgrammerState.class);

    private final ConcurrentMap<Integer, LongAdder> portionsByProgrammer = new ConcurrentHashMap<>();
    private final LongAdder totalPortions = new LongAdder();

    private final long startedAtNanos = System.nanoTime();
    private volatile long lastStateChangeNanos = startedAtNanos;
    private volatile long lastPortionNanos = 0L;

    public SimulationStats() {
        for (ProgrammerState st : ProgrammerState.values()) {
            stateDistribution.put(st, new LongAdder());
        }
    }

    public void onProgrammerStateChange(int programmerId, ProgrammerState newState) {
        Objects.requireNonNull(newState, "newState");
        ProgrammerState prev = states.put(programmerId, newState);
        if (prev != null) {
            stateDistribution.get(prev).decrement();
        }
        stateDistribution.get(newState).increment();
        lastStateChangeNanos = System.nanoTime();
    }

    public void onPortionConsumed(int programmerId) {
        portionsByProgrammer
                .computeIfAbsent(programmerId, k -> new LongAdder())
                .increment();
        totalPortions.increment();
        lastPortionNanos = System.nanoTime();
    }

    public Map<Integer, Long> summaryByProgrammer() {
        Map<Integer, Long> snapshot = new HashMap<>();
        portionsByProgrammer.forEach((id, adder) -> snapshot.put(id, adder.longValue()));
        return Collections.unmodifiableMap(snapshot);
    }

    public Map<ProgrammerState, Long> stateDistribution() {
        Map<ProgrammerState, Long> m = new EnumMap<>(ProgrammerState.class);
        for (Map.Entry<ProgrammerState, LongAdder> e : stateDistribution.entrySet()) {
            m.put(e.getKey(), e.getValue().longValue());
        }
        return Collections.unmodifiableMap(m);
    }

    public long totalConsumed() {
        return totalPortions.longValue();
    }

    @Override
    public String toString() {
        return "SimulationStats{" +
                "totalPortions=" + totalPortions.longValue() +
                ", stateDistribution=" + stateDistribution() +
                ", perProgrammer=" + summaryByProgrammer() +
                '}';
    }
}