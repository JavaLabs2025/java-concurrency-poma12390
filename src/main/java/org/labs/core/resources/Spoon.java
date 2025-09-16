package org.labs.core.resources;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public interface Spoon {
    int id();

    /**
     * Попытаться захватить ложку в течение таймаута.
     * @return true если захвачена, иначе false
     */
    boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException;

    /** Освободить ложку. */
    void release();

    /** Для метрик/отладки – предоставьте доступ к базовой блокировке при необходимости. */
    Optional<Lock> underlyingLock();
}