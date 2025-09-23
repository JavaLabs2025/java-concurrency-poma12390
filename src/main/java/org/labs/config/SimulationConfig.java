package org.labs.config;

import java.time.Duration;
import java.util.Objects;

/**
 * Неизменяемая конфигурация симуляции.
 * Создаётся через {@link Builder}. Все поля валидируются в {@link Builder#build()}.
 */
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

    @Override
    public String toString() {
        return "SimulationConfig{" +
                "programmers=" + programmers +
                ", waiters=" + waiters +
                ", totalPortions=" + totalPortions +
                ", thinkRange=" + minThink + ".." + maxThink +
                ", eatRange=" + minEat + ".." + maxEat +
                ", spoonAcquireTimeout=" + spoonAcquireTimeout +
                ", fairShareRequired=" + fairShareRequired +
                '}';
    }


    public static final class Builder {
        private int programmers = 7;
        private int waiters = 2;
        private long totalPortions = 1000;

        private Duration minThink = Duration.ofMillis(50);
        private Duration maxThink = Duration.ofMillis(200);

        private Duration minEat = Duration.ofMillis(50);
        private Duration maxEat = Duration.ofMillis(200);

        private Duration spoonAcquireTimeout = Duration.ofMillis(250);

        private boolean fairShareRequired = true;

        public Builder programmers(int v) {
            this.programmers = v;
            return this;
        }

        public Builder waiters(int v) {
            this.waiters = v;
            return this;
        }

        public Builder totalPortions(long v) {
            this.totalPortions = v;
            return this;
        }

        public Builder thinkRange(Duration min, Duration max) {
            this.minThink = Objects.requireNonNull(min, "minThink");
            this.maxThink = Objects.requireNonNull(max, "maxThink");
            return this;
        }

        public Builder eatRange(Duration min, Duration max) {
            this.minEat = Objects.requireNonNull(min, "minEat");
            this.maxEat = Objects.requireNonNull(max, "maxEat");
            return this;
        }

        public Builder spoonAcquireTimeout(Duration v) {
            this.spoonAcquireTimeout = Objects.requireNonNull(v, "spoonAcquireTimeout");
            return this;
        }

        public Builder fairShareRequired(boolean v) {
            this.fairShareRequired = v;
            return this;
        }

        public SimulationConfig build() {
            if (programmers < 2) {
                throw new IllegalArgumentException("programmers must be >= 2");
            }
            if (waiters < 1) {
                throw new IllegalArgumentException("waiters must be >= 1");
            }
            if (totalPortions < 1) {
                throw new IllegalArgumentException("totalPortions must be >= 1");
            }

            requireNonNullDurations();

            if (minThink.isNegative() || maxThink.isNegative()) {
                throw new IllegalArgumentException("think durations must be >= 0");
            }
            if (minEat.isNegative() || maxEat.isNegative()) {
                throw new IllegalArgumentException("eat durations must be >= 0");
            }
            if (minThink.compareTo(maxThink) > 0) {
                throw new IllegalArgumentException("minThink must be <= maxThink");
            }
            if (minEat.compareTo(maxEat) > 0) {
                throw new IllegalArgumentException("minEat must be <= maxEat");
            }
            if (spoonAcquireTimeout.isNegative() || spoonAcquireTimeout.isZero()) {
                throw new IllegalArgumentException("spoonAcquireTimeout must be > 0");
            }

            return new SimulationConfig(
                    programmers,
                    waiters,
                    totalPortions,
                    minThink,
                    maxThink,
                    minEat,
                    maxEat,
                    spoonAcquireTimeout,
                    fairShareRequired
            );
        }

        private void requireNonNullDurations() {
            Objects.requireNonNull(minThink, "minThink");
            Objects.requireNonNull(maxThink, "maxThink");
            Objects.requireNonNull(minEat, "minEat");
            Objects.requireNonNull(maxEat, "maxEat");
            Objects.requireNonNull(spoonAcquireTimeout, "spoonAcquireTimeout");
        }
    }
}