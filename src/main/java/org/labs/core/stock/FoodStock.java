package org.labs.core.stock;

public interface FoodStock {
    /**
     * Попытаться выдать 1 порцию. Возвращает true, если удалось уменьшить запас.
     */
    boolean tryTakeOne();

    /** Сколько порций осталось. */
    long remaining();

    /** Признак завершения – запаса больше нет. */
    default boolean isDepleted() { return remaining() <= 0; }
}