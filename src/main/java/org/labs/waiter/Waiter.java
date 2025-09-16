package org.labs.waiter;


import org.labs.model.RefillRequest;

public interface Waiter extends Runnable {
    int id();

    /** Обрабатывает один запрос (внутри run-цикла). */
    void handle(RefillRequest request);

    /** Завершить работу (graceful). */
    void shutdown();
}