package org.labs.strategy.deadlock;

import org.junit.jupiter.api.Test;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NMinusOneDeadlockStrategyTest {

    @Test
    void permitsAtMostNMinusOneToEnter() throws Exception {
        int n = 5;
        NMinusOneDeadlockStrategy s = new NMinusOneDeadlockStrategy(n, true);

        for (int i = 0; i < n - 1; i++) {
            s.beforeAcquire(i);
        }
        assertEquals(0, s.availableSlots());

        CountDownLatch entered = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                s.beforeAcquire(99);
                entered.countDown();
            } catch (InterruptedException ignored) { }
        });
        t.start();

        Thread.sleep(20);
        assertEquals(1, entered.getCount());

        s.afterRelease(0);

        assertTrue(entered.await(200, TimeUnit.MILLISECONDS));

        s.afterRelease(99);
        for (int i = 1; i < n - 1; i++) {
            s.afterRelease(i);
        }
    }
}
