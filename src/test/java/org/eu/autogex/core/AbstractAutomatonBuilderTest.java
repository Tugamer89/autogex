package org.eu.autogex.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AbstractAutomatonBuilderTest {

    // A concrete subclass for testing the abstract builder logic
    static class TestBuilder extends AbstractAutomatonBuilder<TestBuilder, Automaton> {
        @Override
        protected TestBuilder self() {
            return this;
        }

        @Override
        public Automaton build() {
            return null; // Not needed for this test
        }

        // Public wrapper to test the protected method
        public State[] testGetTransitionStatesOrThrow(String fromName, String toName) {
            return getTransitionStatesOrThrow(fromName, toName);
        }
    }

    @Test
    void testGetTransitionStatesOrThrow_BothExist() {
        TestBuilder builder = new TestBuilder();
        builder.addState("q0", false);
        builder.addState("q1", true);

        State[] states = builder.testGetTransitionStatesOrThrow("q0", "q1");

        assertNotNull(states);
        assertEquals(2, states.length);
        assertEquals("q0", states[0].getName());
        assertEquals("q1", states[1].getName());
    }

    @Test
    void testGetTransitionStatesOrThrow_FromMissing() {
        TestBuilder builder = new TestBuilder();
        builder.addState("q1", true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            builder.testGetTransitionStatesOrThrow("q0", "q1");
                        });

        assertEquals("State not found. Add it first using addState.", exception.getMessage());
    }

    @Test
    void testGetTransitionStatesOrThrow_ToMissing() {
        TestBuilder builder = new TestBuilder();
        builder.addState("q0", false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            builder.testGetTransitionStatesOrThrow("q0", "q1");
                        });

        assertEquals("State not found. Add it first using addState.", exception.getMessage());
    }

    @Test
    void testGetTransitionStatesOrThrow_BothMissing() {
        TestBuilder builder = new TestBuilder();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            builder.testGetTransitionStatesOrThrow("q0", "q1");
                        });

        assertEquals("State not found. Add it first using addState.", exception.getMessage());
    }
}
