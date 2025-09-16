package org.labs.config;

import java.time.Duration;

public final class SimulationConfig {
    private final int programmers;
    private final int waiters;
    private final long totalPortions;
    private final Duration minThink;
    private final Duration maxThink;
    private final Duration minEat;
    private final Duration maxEat;
    private final Duration spoonAcquireTimeout;
    private final boolean fairShareRequired;

    private SimulationConfig(
            int programmers,
            int waiters,
            long totalPortions,
            Duration minThink,
            Duration maxThink,
            Duration minEat,
            Duration maxEat,
            Duration spoonAcquireTimeout,
            boolean fairShareRequired
    ) {
        this.programmers = programmers;
        this.waiters = waiters;
        this.totalPortions = totalPortions;
        this.minThink = minThink;
        this.maxThink = maxThink;
        this.minEat = minEat;
        this.maxEat = maxEat;
        this.spoonAcquireTimeout = spoonAcquireTimeout;
        this.fairShareRequired = fairShareRequired;
    }

    public int programmers() { return programmers; }
    public int waiters() { return waiters; }
    public long totalPortions() { return totalPortions; }
    public Duration minThink() { return minThink; }
    public Duration maxThink() { return maxThink; }
    public Duration minEat() { return minEat; }
    public Duration maxEat() { return maxEat; }
    public Duration spoonAcquireTimeout() { return spoonAcquireTimeout; }
    public boolean fairShareRequired() { return fairShareRequired; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int programmers = 7;
        private int waiters = 2;
        private long totalPortions = 1_000_000L;
        private Duration minThink = Duration.ofMillis(50);
        private Duration maxThink = Duration.ofMillis(200);
        private Duration minEat   = Duration.ofMillis(50);
        private Duration maxEat   = Duration.ofMillis(200);
        private Duration spoonAcquireTimeout = Duration.ofMillis(250);
        private boolean fairShareRequired = true;

        public Builder programmers(int v) { this.programmers = v; return this; }
        public Builder waiters(int v) { this.waiters = v; return this; }
        public Builder totalPortions(long v) { this.totalPortions = v; return this; }
        public Builder thinkRange(Duration min, Duration max) { this.minThink = min; this.maxThink = max; return this; }
        public Builder eatRange(Duration min, Duration max) { this.minEat = min; this.maxEat = max; return this; }
        public Builder spoonAcquireTimeout(Duration v) { this.spoonAcquireTimeout = v; return this; }
        public Builder fairShareRequired(boolean v) { this.fairShareRequired = v; return this; }

        public SimulationConfig build() {
            // validate inputs (N>1, waiters>0, etc.)
            return new SimulationConfig(
                    programmers, waiters, totalPortions, minThink, maxThink, minEat, maxEat, spoonAcquireTimeout, fairShareRequired
            );
        }
    }
}