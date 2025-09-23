package org.labs.waiter;

import org.labs.core.stock.FoodStock;
import org.labs.model.RefillRequest;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class DefaultWaiter implements Waiter {
    private static final long POLL_TIMEOUT_MS = 250L;

    private final int id;
    private final BlockingQueue<RefillRequest> queue;
    private final FoodStock stock;

    private volatile boolean running = true;

    public DefaultWaiter(int id, BlockingQueue<RefillRequest> queue, FoodStock stock) {
        if (id < 0) throw new IllegalArgumentException("id must be >= 0");
        this.id = id;
        this.queue = Objects.requireNonNull(queue, "queue");
        this.stock = Objects.requireNonNull(stock, "stock");
    }

    @Override
    public void run() {
        try {
            while (running || !queue.isEmpty()) {
                try {
                    RefillRequest req = queue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    if (req != null) {
                        handle(req);
                    }
                } catch (InterruptedException ie) {
                    if (!running) {
                        break;
                    }
                }
            }
        } finally {
            RefillRequest req;
            while ((req = queue.poll()) != null) {
                safeComplete(req, false);
            }
        }
    }

    @Override
    public void handle(RefillRequest request) {
        boolean granted = stock.tryTakeOne();
        safeComplete(request, granted);
    }

    @Override
    public void shutdown() {
        running = false;
    }

    private static void safeComplete(RefillRequest request, boolean result) {
        try {
            request.tryComplete(result);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public String toString() {
        return "DefaultWaiter{id=" + id + ", running=" + running + '}';
    }
}