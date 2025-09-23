package org.labs.strategy.fairness;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class EqualShareFairnessTest {

    @Test
    void slackZeroStrictEquality() {
        EqualShareFairness f = new EqualShareFairness(0);

        assertTrue(f.tryEnterEat(1, 0));
        assertFalse(f.tryEnterEat(2, 1));

        f.onFinishEat(1);

        assertFalse(f.tryEnterEat(1, 1));
        assertTrue(f.tryEnterEat(2, 0));
    }

    @Test
    void slackOneAllowsLeadByOne() {
        EqualShareFairness f = new EqualShareFairness(1);

        assertTrue(f.tryEnterEat(1, 0));
        assertTrue(f.tryEnterEat(2, 0));

        f.onFinishEat(1); // 1-й завершил одну

        assertTrue(f.tryEnterEat(1, 1));
        assertTrue(f.tryEnterEat(2, 0));

        f.onFinishEat(1); // #1 теперь 2
        assertFalse(f.tryEnterEat(1, 2));
    }
}
