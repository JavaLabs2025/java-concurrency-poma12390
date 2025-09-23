package org.labs.core.resources;

import java.util.concurrent.TimeUnit;

public interface Spoon {
    int id();

    /**
     * Попытаться захватить ложку в течение таймаута
     * @return true если захвачена, иначе false
     */
    boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException;

    /** Освободить ложку */
    void release();
}