package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.State;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DFATest {

    @Test
    void testDFAAcceptsStringsWithAtLeastOneZero() {
        // Example 2.1.3
        // Accepts strings containing at least one '0'
        DFA dfa = new DFA.Builder()
                .addState("q0", false)
                .addState("q1", true)   // q1 is the only final state
                .addState("q2", false)
                .setInitialState("q0")
                // Transitions from q0
                .addTransition("q0", '0', "q1")
                .addTransition("q0", '1', "q2")
                // Transitions from q1
                .addTransition("q1", '0', "q1")
                .addTransition("q1", '1', "q1")
                // Transitions from q2
                .addTransition("q2", '0', "q1")
                .addTransition("q2", '1', "q0")
                .build();

        // ACCEPTED
        assertTrue(dfa.accepts("0"), "Should accept a single 0");
        assertTrue(dfa.accepts("100"), "Should accept 100");
        assertTrue(dfa.accepts("11110"), "Should accept 11110");
        assertTrue(dfa.accepts("10101"), "Should accept 10101");

        // REJECTED
        assertFalse(dfa.accepts("11"), "Should not accept 11");
        assertFalse(dfa.accepts("1"), "Should not accept 1");
        assertFalse(dfa.accepts("1111111"), "Should not accept only 1s");
        assertFalse(dfa.accepts("10a"), "Should not accept 10a");
        assertFalse(dfa.accepts(""), "Should not accept the empty string");
    }

    @Test
    void testDFAGetters() {
        // Tests the getter methods
        DFA dfa = new DFA.Builder()
                .addState("q0", false)
                .addState("q1", true)
                .setInitialState("q0")
                .addTransition("q0", 'a', "q1")
                .build();

        Set<State> states = dfa.getStates();
        assertEquals(2, states.size(), "There should be exactly 2 states");

        State initial = dfa.getInitialState();
        assertNotNull(initial);
        assertEquals("q0", initial.getName(), "The initial state must be q0");

        Set<State> finalStates = dfa.getFinalStates();
        assertEquals(1, finalStates.size(), "There should be exactly 1 final state");
        assertTrue(finalStates.iterator().next().isFinal(), "The state in finalStates must be final");
    }

    @Test
    void testBuilderThrowsIllegalArgumentExceptionForMissingState() {
        // Tests that an exception is thrown if adding a transition with non-existing states
        DFA.Builder builder = new DFA.Builder().addState("q0", false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addTransition("q0", 'a', "q1"); // "q1" was never added
        });

        assertTrue(exception.getMessage().contains("State not found"), "The error message must mention the missing state");
    }

    @Test
    void testBuilderThrowsIllegalStateExceptionForMissingInitialState() {
        // Tests that a DFA cannot be built without setting the initial state
        DFA.Builder builder = new DFA.Builder()
                .addState("q0", false)
                .addState("q1", true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

        assertTrue(exception.getMessage().contains("initial state"), "The error message must mention the initial state");
    }
}