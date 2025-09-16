package org.labs.metrics;

import org.labs.model.ProgrammerState;

import java.util.Map;

public final class SimulationStats {
    /** Кол-во начавших есть/закончивших, очереди, отказов из-за отсутствия еды и т.д. */
    // Добавьте необходимые счетчики, таймеры, гистограммы latency по желанию.

    public void onProgrammerStateChange(int programmerId, ProgrammerState newState) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    public void onPortionConsumed(int programmerId) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    public Map<Integer, Long> summaryByProgrammer() {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }
}