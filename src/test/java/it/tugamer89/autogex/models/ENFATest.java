package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.State;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ENFATest {

    @Test
    void testENFAWithEpsilonTransitions() {
        // Automaton that recognizes "a*b*" leveraging ε-transitions
        ENFA enfa = new ENFA.Builder()
                .addState("q0", true)
                .addState("q1", true)
                .setInitialState("q0")
                .addTransition("q0", 'a', "q0")
                .addEpsilonTransition("q0", "q1") // q0 --ε--> q1
                .addTransition("q1", 'b', "q1")
                .build();

        // ACCEPTED
        assertTrue(enfa.accepts(""), "Accepts empty string (q0 is final)");
        assertTrue(enfa.accepts("a"), "Accepts 'a'");
        assertTrue(enfa.accepts("b"), "Accepts 'b' (ε-jump to q1)");
        assertTrue(enfa.accepts("ab"), "Accepts 'ab'");
        assertTrue(enfa.accepts("aaaabbbb"), "Accepts 'aaaabbbb'");

        // REJECTED
        assertFalse(enfa.accepts("ba"), "Rejects 'ba' (cannot jump back from q1)");
        assertFalse(enfa.accepts("aba"), "Rejects 'aba'");
        assertFalse(enfa.accepts("c"), "Rejects 'c'");
    }

    @Test
    void testENFAGetters() {
        // Tests the getter methods
        ENFA enfa = new ENFA.Builder()
                .addState("q0", true)
                .setInitialState("q0")
                .build();

        Set<State> states = enfa.getStates();
        assertEquals(1, states.size(), "There should be exactly 1 state");
        
        State initial = enfa.getInitialState();
        assertNotNull(initial);
        assertEquals("q0", initial.getName(), "The initial state must be q0");
        
        Set<State> finalStates = enfa.getFinalStates();
        assertEquals(1, finalStates.size(), "There should be exactly 1 final state");
        assertTrue(finalStates.iterator().next().isFinal(), "The state in finalStates must be final");
    }

    @Test
    void testBuilderThrowsIllegalArgumentExceptionForMissingState() {
        // Tests that an exception is thrown if adding a transition with non-existing states
        ENFA.Builder builder = new ENFA.Builder().addState("q0", false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addEpsilonTransition("q0", "qX"); // "qX" was never added
        });

        assertTrue(exception.getMessage().contains("State not found"), "The error message must mention the missing state");
    }

    @Test
    void testBuilderThrowsIllegalStateExceptionForMissingInitialState() {
        // Tests that an ENFA cannot be built without setting the initial state
        ENFA.Builder builder = new ENFA.Builder().addState("q0", false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

        assertTrue(exception.getMessage().contains("initial state"), "The error message must mention the initial state");
    }
}