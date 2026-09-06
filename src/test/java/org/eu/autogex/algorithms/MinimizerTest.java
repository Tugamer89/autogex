package org.eu.autogex.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.eu.autogex.models.DFA;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MinimizerTest {

    @ParameterizedTest(name = "Testing Minimized DFA behavior with input: ''{0}''")
    @ValueSource(strings = {"a", "b", "abbb", "aa", "bbaa", "ababa", "", "bbbb"})
    void testMinimizeRedundantDFA(String input) {
        // States q1 and q2 are completely equivalent.
        // State q5 is unreachable.
        DFA redundantDfa =
                new DFA.Builder()
                        .addState("q0", false)
                        .addState("q1", true)
                        .addState("q2", true)
                        .addState("q3", false)
                        .addState("q5", true)
                        .setInitialState("q0")

                        // Transitions from q0 go to q1 and q2
                        .addTransition("q0", 'a', "q1")
                        .addTransition("q0", 'b', "q2")

                        // q1 and q2 both go to q3 with 'a' and loop on themselves with 'b'
                        .addTransition("q1", 'a', "q3")
                        .addTransition("q1", 'b', "q1")
                        .addTransition("q2", 'a', "q3")
                        .addTransition("q2", 'b', "q2")

                        // q3 goes back to q0
                        .addTransition("q3", 'a', "q0")
                        .addTransition("q3", 'b', "q0")
                        .build();

        assertEquals(5, redundantDfa.getStates().size());

        DFA minimalDfa = Minimizer.minimize(redundantDfa);

        // Expected total states: q0_new, q1_q2_new, q3_new -> 3 states!
        assertEquals(
                3, minimalDfa.getStates().size(), "The minimal DFA must have exactly 3 states");

        // Verify that the languages match
        assertEquals(
                redundantDfa.accepts(input),
                minimalDfa.accepts(input),
                "Both DFAs must behave identically");
    }

    @ParameterizedTest(name = "Testing Already Minimal DFA behavior with input: ''{0}''")
    @ValueSource(strings = {"a", "b", "abbb", "aa", "bbaa", "ababa", "", "bbbb"})
    void testMinimizeAlreadyMinimalDFA(String input) {
        DFA alreadyMinimalDfa =
                new DFA.Builder()
                        .addState("q0", true)
                        .addState("q1", false)
                        .setInitialState("q0")
                        .addTransition("q0", 'a', "q0")
                        .addTransition("q0", 'b', "q1")
                        .addTransition("q1", 'a', "q1")
                        .addTransition("q1", 'b', "q1")
                        .build();

        assertEquals(2, alreadyMinimalDfa.getStates().size());

        DFA minimizedDfa = Minimizer.minimize(alreadyMinimalDfa);

        assertEquals(
                2, minimizedDfa.getStates().size(), "The DFA should remain at exactly 2 states");
        assertEquals(
                alreadyMinimalDfa.accepts(input),
                minimizedDfa.accepts(input),
                "Both DFAs must behave identically");
    }

    @Test
    void testUtilityClassConstructorThrowsException() throws Exception {
        Constructor<Minimizer> constructor = Minimizer.class.getDeclaredConstructor();
        assertTrue(
                java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()),
                "Constructor should be private for utility classes");

        constructor.setAccessible(true);
        InvocationTargetException exception =
                assertThrows(
                        InvocationTargetException.class,
                        constructor::newInstance,
                        "Instantiating the utility class should throw an exception");
        assertTrue(
                exception.getCause() instanceof UnsupportedOperationException,
                "The cause should be an UnsupportedOperationException");
        assertTrue(
                exception.getCause().getMessage().contains("Utility class"),
                "Exception message should mention utility class");
    }
}
