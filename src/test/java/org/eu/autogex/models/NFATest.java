package org.eu.autogex.models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.eu.autogex.core.State;
import org.junit.jupiter.api.Test;

class NFATest {

    @Test
    void testNFAAcceptsPenultimateZero() {
        // Example 2.1.6
        // Language: strings over {0,1} where the PENULTIMATE symbol is 0.
        NFA nfa =
                new NFA.Builder()
                        .addState("q0", false)
                        .addState("q1", false)
                        .addState("q2", true)
                        .setInitialState("q0")
                        // Transitions from q0
                        .addTransition("q0", '1', "q0")
                        .addTransition("q0", '0', "q0")
                        .addTransition("q0", '0', "q1")
                        // Transitions from q1
                        .addTransition("q1", '0', "q2")
                        .addTransition("q1", '1', "q2")
                        .build();

        // ACCEPTED
        assertTrue(nfa.accepts("00"), "Accepts '00' (penultimate is 0)");
        assertTrue(nfa.accepts("01"), "Accepts '01'");
        assertTrue(nfa.accepts("10101"), "Accepts '10101'");
        assertTrue(nfa.accepts("111100"), "Accepts '111100'");

        // REJECTED
        assertFalse(nfa.accepts("0"), "Rejects '0' (too short)");
        assertFalse(nfa.accepts("10"), "Rejects '10' (penultimate is 1)");
        assertFalse(nfa.accepts("111"), "Rejects '111'");
        assertFalse(nfa.accepts(""), "Rejects empty string");
    }

    @Test
    void testNFAGetters() {
        // Tests the getter methods
        NFA nfa = new NFA.Builder().addState("q0", true).setInitialState("q0").build();

        Set<State> states = nfa.getStates();
        assertEquals(1, states.size(), "There should be exactly 1 state");

        State initial = nfa.getInitialState();
        assertNotNull(initial);
        assertEquals("q0", initial.getName(), "The initial state must be q0");

        Set<State> finalStates = nfa.getFinalStates();
        assertEquals(1, finalStates.size(), "There should be exactly 1 final state");
        assertTrue(
                finalStates.iterator().next().isFinal(), "The state in finalStates must be final");
    }

    @Test
    void testBuilderThrowsIllegalArgumentExceptionForMissingState() {
        // Tests that an exception is thrown if adding a transition with non-existing states
        NFA.Builder builder = new NFA.Builder().addState("q0", false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            builder.addTransition("q0", 'a', "qX"); // "qX" was never added
                        });

        assertTrue(
                exception.getMessage().contains("State not found"),
                "The error message must mention the missing state");
    }

    @Test
    void testBuilderThrowsIllegalStateExceptionForMissingInitialState() {
        // Tests that an NFA cannot be built without setting the initial state
        NFA.Builder builder = new NFA.Builder().addState("q0", false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

        assertTrue(
                exception.getMessage().contains("initial state"),
                "The error message must mention the initial state");
    }
}
