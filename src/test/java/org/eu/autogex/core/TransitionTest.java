package org.eu.autogex.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TransitionTest {

    @Test
    void testIsEpsilon() {
        State q0 = new State("q0", false);
        State q1 = new State("q1", true);

        // Normal transition with a symbol
        Transition normalTransition = new Transition(q0, 'a', q1);
        assertFalse(
                normalTransition.isEpsilon(),
                "A transition with a non-null symbol must not be considered epsilon");

        // Epsilon transition (null symbol)
        Transition epsilonTransition = new Transition(q0, null, q1);
        assertTrue(
                epsilonTransition.isEpsilon(),
                "A transition with a null symbol must be considered epsilon");
    }

    @Test
    void testToString() {
        State q0 = new State("q0", false);
        State q1 = new State("q1", true);

        Transition normalTransition = new Transition(q0, 'b', q1);
        Transition epsilonTransition = new Transition(q0, null, q1);

        // Verify formatting for standard transitions
        assertEquals(
                "q0 --b--> q1",
                normalTransition.toString(),
                "The toString must correctly format the transition with state names and symbol");

        // Verify formatting for epsilon transitions
        assertEquals(
                "q0 --ε--> q1",
                epsilonTransition.toString(),
                "The toString must replace null with 'ε' for epsilon transitions");
    }

    @Test
    void testRecordAccessors() {
        State startState = new State("start", false);
        State endState = new State("end", true);

        Transition transition = new Transition(startState, 'x', endState);

        // Verify that the record accessor methods work as expected
        assertEquals(
                startState,
                transition.from(),
                "The from() accessor must return the correct start state");
        assertEquals(
                'x', transition.symbol(), "The symbol() accessor must return the correct symbol");
        assertEquals(
                endState,
                transition.to(),
                "The to() accessor must return the correct destination state");
    }
}
