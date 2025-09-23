package org.labs.core.resources;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class LockingSpoonTest {

    @Test
    void tryAcquireAndReleaseSameThread() throws Exception {
        Spoon s = new LockingSpoon(1);
        assertTrue(s.tryAcquire(10, TimeUnit.MILLISECONDS));
        s.release();
    }

    @Test
    void tryAcquireTimesOutWhenHeld() throws Exception {
        Spoon s = new LockingSpoon(2);
        Thread t = new Thread(() -> {
            try {
                assertTrue(s.tryAcquire(0, TimeUnit.MILLISECONDS));
                Thread.sleep(50);
                s.release();
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        t.start();
        Thread.sleep(5);
        assertFalse(s.tryAcquire(1, TimeUnit.MILLISECONDS));
        t.join();
        assertTrue(s.tryAcquire(10, TimeUnit.MILLISECONDS));
        s.release();
    }

    @Test
    void releaseByNonOwnerThrows() throws Exception {
        Spoon s = new LockingSpoon(3);
        assertTrue(s.tryAcquire(10, TimeUnit.MILLISECONDS));
        Thread t = new Thread(() -> assertThrows(IllegalStateException.class, s::release));
        t.start(); t.join();
        s.release();
    }
}
