package org.labs.waiter;

import org.junit.jupiter.api.Test;
import org.labs.core.stock.AtomicFoodStock;
import org.labs.core.stock.FoodStock;
import org.labs.model.RefillRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultWaiterTest {

    @Test
    void grantsUntilStockEmptyThenDenies() throws Exception {
        FoodStock stock = new AtomicFoodStock(2);
        BlockingQueue<RefillRequest> q = new LinkedBlockingQueue<>();
        DefaultWaiter w = new DefaultWaiter(1, q, stock);
        Thread t = new Thread(w, "waiter-test");
        t.start();

        RefillRequest r1 = new RefillRequest(10);
        RefillRequest r2 = new RefillRequest(11);
        RefillRequest r3 = new RefillRequest(12);

        q.put(r1); q.put(r2); q.put(r3);

        assertTrue(r1.resultFuture().get(1, TimeUnit.SECONDS));
        assertTrue(r2.resultFuture().get(1, TimeUnit.SECONDS));
        assertFalse(r3.resultFuture().get(1, TimeUnit.SECONDS));

        w.shutdown();
        t.interrupt();
        t.join(1000);
        assertFalse(t.isAlive());
    }
}
