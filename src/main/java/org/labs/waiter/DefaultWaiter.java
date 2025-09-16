package org.labs.waiter;

import org.labs.core.stock.FoodStock;
import org.labs.model.RefillRequest;

import java.util.concurrent.BlockingQueue;

final class DefaultWaiter implements Waiter {
    private final int id;
    private final BlockingQueue<RefillRequest> queue;
    private final FoodStock stock;
    private volatile boolean running = true;

    public DefaultWaiter(int id, BlockingQueue<RefillRequest> queue, FoodStock stock) {
        this.id = id;
        this.queue = queue;
        this.stock = stock;
    }

    @Override
    public int id() { return id; }

    @Override
    public void run() {
        // TODO: poll из queue, handle(request) пока running
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void handle(RefillRequest request) {
        // TODO: попытаться выдать 1 порцию из stock, уведомить программиста при успехе/неуспехе
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void shutdown() {
        // TODO: остановка цикла + очистка
        throw new UnsupportedOperationException("Not implemented");
    }
}