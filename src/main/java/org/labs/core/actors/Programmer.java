package org.labs.core.actors;

import org.labs.config.SimulationConfig;
import org.labs.core.resources.Spoon;
import org.labs.model.ProgrammerState;
import org.labs.model.RefillRequest;
import org.labs.strategy.deadlock.DeadlockAvoidanceStrategy;
import org.labs.strategy.fairness.FairnessStrategy;

import java.util.concurrent.BlockingQueue;

public final class Programmer implements Runnable {
    private final int id;
    private final Spoon left;
    private final Spoon right;
    private final SimulationConfig cfg;
    private final DeadlockAvoidanceStrategy deadlockStrategy;
    private final FairnessStrategy fairness;
    private final BlockingQueue<RefillRequest> refillQueue;
    private volatile ProgrammerState state = ProgrammerState.THINKING;
    private long portionsEaten;

    public Programmer(
            int id,
            Spoon left,
            Spoon right,
            SimulationConfig cfg,
            DeadlockAvoidanceStrategy deadlockStrategy,
            FairnessStrategy fairness,
            BlockingQueue<RefillRequest> refillQueue
    ) {
        this.id = id;
        this.left = left;
        this.right = right;
        this.cfg = cfg;
        this.deadlockStrategy = deadlockStrategy;
        this.fairness = fairness;
        this.refillQueue = refillQueue;
    }

    public int id() { return id; }
    public ProgrammerState state() { return state; }
    public long portionsEaten() { return portionsEaten; }

    @Override
    public void run() {
        // TODO: цикл пока есть еда в системе;
        // - think()
        // - deadlockStrategy.beforeAcquire(id)
        // - попытка взять две ложки (учитывая таймаут из cfg)
        // - fairness.tryEnterEat(...)
        // - eat()
        // - освободить ложки + deadlockStrategy.afterRelease(id)
        // - если суп кончился в тарелке – requestRefill()
        // Логи/метрики по состояниям.
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Подумать/поболтать. */
    void think() {
        // TODO: sleep в диапазоне cfg.minThink..cfg.maxThink
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Пожевать супчик (пока у программиста есть порция в тарелке). */
    void eat() {
        // TODO: sleep в диапазоне cfg.minEat..cfg.maxEat; увеличить portionsEaten
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Попросить долив у официанта. */
    void requestRefill() {
        // TODO: добавить RefillRequest в refillQueue
        throw new UnsupportedOperationException("Not implemented");
    }
}