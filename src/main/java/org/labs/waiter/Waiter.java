package org.labs.waiter;


import org.labs.model.RefillRequest;

public interface Waiter extends Runnable {
    void handle(RefillRequest request);

    void shutdown();
}