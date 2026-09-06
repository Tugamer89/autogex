package org.eu.autogex.trace;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.eu.autogex.core.State;
import org.junit.jupiter.api.Test;

class ExecutionStepTest {

    @Test
    void testCreationAndRetrieval() {
        State q0 = new State("q0", false);
        State q1 = new State("q1", true);

        Set<State> fromStates = Set.of(q0);
        Set<State> toStates = Set.of(q1);

        ExecutionStep step = new ExecutionStep(fromStates, 'a', toStates);

        assertEquals(fromStates, step.fromStates());
        assertEquals('a', step.symbolRead());
        assertEquals(toStates, step.toStates());
    }

    @Test
    void testToStringNormalTransition() {
        State q0 = new State("q0", false);
        State q1 = new State("q1", true);

        ExecutionStep step = new ExecutionStep(Set.of(q0), 'a', Set.of(q1));

        assertEquals("{q0} --[a]--> {q1}", step.toString());
    }

    @Test
    void testToStringEpsilonTransition() {
        State q0 = new State("q0", false);
        State q1 = new State("q1", true);

        ExecutionStep step = new ExecutionStep(Set.of(q0), null, Set.of(q1));

        assertEquals("{q0} --[ε]--> {q1}", step.toString());
    }

    @Test
    void testToStringMultipleStatesSorting() {
        State q2 = new State("q2", true);
        State q0 = new State("q0", false);
        State q1 = new State("q1", false);

        Set<State> fromStates = new HashSet<>();
        fromStates.add(q2);
        fromStates.add(q0);
        fromStates.add(q1);

        Set<State> toStates = Set.of(q2);

        ExecutionStep step = new ExecutionStep(fromStates, 'b', toStates);

        assertEquals("{q0, q1, q2} --[b]--> {q2}", step.toString());
    }

    @Test
    void testToStringEmptySet() {
        State q0 = new State("q0", false);

        ExecutionStep step = new ExecutionStep(Set.of(q0), 'c', Collections.emptySet());

        assertEquals("{q0} --[c]--> ∅", step.toString());

        ExecutionStep stepFromEmpty = new ExecutionStep(Collections.emptySet(), 'c', Set.of(q0));
        assertEquals("∅ --[c]--> {q0}", stepFromEmpty.toString());
    }

    @Test
    void testToStringNullSet() {
        State q0 = new State("q0", false);

        ExecutionStep step = new ExecutionStep(Set.of(q0), 'd', null);

        assertEquals("{q0} --[d]--> ∅", step.toString());

        ExecutionStep stepFromNull = new ExecutionStep(null, 'd', Set.of(q0));
        assertEquals("∅ --[d]--> {q0}", stepFromNull.toString());
    }
}
