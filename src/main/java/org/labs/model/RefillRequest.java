package org.labs.model;

public final class RefillRequest {
    private final int programmerId;
    private final long createdAtNanos;

    public RefillRequest(int programmerId, long createdAtNanos) {
        this.programmerId = programmerId;
        this.createdAtNanos = createdAtNanos;
    }

    public int programmerId() { return programmerId; }
    public long createdAtNanos() { return createdAtNanos; }
}