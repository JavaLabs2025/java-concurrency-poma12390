package org.labs.model;

import java.util.concurrent.CompletableFuture;

public final class RefillRequest {
    private final CompletableFuture<Boolean> result;

    public RefillRequest(int programmerId) {
        if (programmerId < 0) {
            throw new IllegalArgumentException("programmerId must be >= 0");
        }
        this.result = new CompletableFuture<>();
    }

    public CompletableFuture<Boolean> resultFuture() {
        return result;
    }

    public void tryComplete(boolean success) {
        result.complete(success);
    }

    public boolean awaitResult() throws InterruptedException {
        try {
            return result.get();
        } catch (InterruptedException ie) {
            throw ie;
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected completion state", e);
        }
    }

}