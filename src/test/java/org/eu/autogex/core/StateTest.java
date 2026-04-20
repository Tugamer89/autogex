package org.eu.autogex.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateTest {

    @Test
    void testStateEqualsAndHashCode() {
        State s1 = new State("q0", false);
        State s2 = new State("q0", false);
        State s3 = new State("q1", true);
        Object nullObject = null;

        assertEquals(s1, s2, "States with the same name and final flag must be equal");
        assertEquals(s1.hashCode(), s2.hashCode(), "Equal states must have the same hashCode");
        assertNotEquals(s1, s3, "Different states must not be equal");
        assertNotEquals(s1, nullObject, "State must not be equal to null");
        assertNotEquals(s1, new Object(), "State must not be equal to an object of a different class");
    }

    @Test
    void testStateToString() {
        State s1 = new State("q0", false);
        State s3 = new State("q1", true);

        assertEquals("q0", s1.toString(), "The toString of a non-final state is just its name");
        assertEquals("*q1", s3.toString(), "The toString of a final state includes an asterisk");
    }
}