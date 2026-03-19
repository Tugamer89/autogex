package it.tugamer89.autogex.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StateTest {

    @Test
    void testStateEqualsAndHashCode() {
        State s1 = new State("q0", false);
        State s2 = new State("q0", false);
        State s3 = new State("q1", true);

        assertEquals(s1, s2, "Stati con stesso nome e flag final devono essere uguali");
        assertEquals(s1.hashCode(), s2.hashCode(), "Stati uguali devono avere stesso hashCode");
        assertNotEquals(s1, s3, "Stati diversi non devono essere uguali");
    }

    @Test
    void testStateToString() {
        State s1 = new State("q0", false);
        State s3 = new State("q1", true);

        assertEquals("q0", s1.toString(), "Il toString di uno stato non finale è solo il nome");
        assertEquals("*q1", s3.toString(), "Il toString di uno stato finale include un asterisco");
    }
}