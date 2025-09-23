package org.labs.strategy.deadlock;

import org.junit.jupiter.api.Test;
import org.labs.core.resources.LockingSpoon;
import org.labs.core.resources.Spoon;

import static org.junit.jupiter.api.Assertions.assertEquals;


class OrderedSpoonsDeadlockStrategyTest {

    @Test
    void ordersById() {
        OrderedSpoonsDeadlockStrategy st = new OrderedSpoonsDeadlockStrategy();
        Spoon a = new LockingSpoon(10);
        Spoon b = new LockingSpoon(3);

        assertEquals(b, st.first(a, b));
        assertEquals(a, st.second(a, b));

        var pair = st.order(a, b);
        assertEquals(3, pair.first().id());
        assertEquals(10, pair.second().id());
    }

    @Test
    void noOpsDontThrow() {
        OrderedSpoonsDeadlockStrategy st = new OrderedSpoonsDeadlockStrategy();
        st.beforeAcquire(1);
        st.afterRelease(1);
    }
}
