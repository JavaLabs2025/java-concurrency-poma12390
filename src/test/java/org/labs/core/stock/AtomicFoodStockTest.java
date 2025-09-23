package org.labs.core.stock;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AtomicFoodStockTest {

    @Test
    void decrementsToZeroWithoutGoingNegative() {
        FoodStock stock = new AtomicFoodStock(3);
        assertTrue(stock.tryTakeOne());
        assertTrue(stock.tryTakeOne());
        assertTrue(stock.tryTakeOne());
        assertFalse(stock.tryTakeOne());
        assertEquals(0, stock.remaining());
        assertTrue(stock.isDepleted());
    }

    @Test
    void concurrentConsumersDontLoseOrDuplicate() throws Exception {
        int threads = 8;
        long portions = 1000;
        FoodStock stock = new AtomicFoodStock(portions);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicLong consumed = new AtomicLong();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    while (stock.tryTakeOne()) {
                        consumed.incrementAndGet();
                    }
                } catch (InterruptedException ignored) { }
                done.countDown();
            }).start();
        }
        start.countDown();
        done.await();

        assertEquals(portions, consumed.get());
        assertEquals(0, stock.remaining());
        assertTrue(stock.isDepleted());
    }
}
