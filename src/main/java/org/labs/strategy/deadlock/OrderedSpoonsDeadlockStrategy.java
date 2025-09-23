package org.labs.strategy.deadlock;

import org.labs.core.resources.Spoon;

import java.util.Objects;

public final class OrderedSpoonsDeadlockStrategy implements DeadlockAvoidanceStrategy {

    public OrderedSpoonsDeadlockStrategy() { }

    @Override
    public void beforeAcquire(int programmerId) {
        // no-op
    }

    @Override
    public void afterRelease(int programmerId) {
        // no-op
    }

    public Spoon first(Spoon left, Spoon right) {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        return left.id() <= right.id() ? left : right;
    }

    public Spoon second(Spoon left, Spoon right) {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        return left.id() <= right.id() ? right : left;
    }

    public OrderedPair order(Spoon a, Spoon b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        return a.id() <= b.id() ? new OrderedPair(a, b) : new OrderedPair(b, a);
    }

    public static final class OrderedPair {
        private final Spoon first;
        private final Spoon second;

        private OrderedPair(Spoon first, Spoon second) {
            this.first = first;
            this.second = second;
        }

        public Spoon first()  { return first;  }
        public Spoon second() { return second; }
    }
}