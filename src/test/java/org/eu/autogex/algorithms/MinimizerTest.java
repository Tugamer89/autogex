package org.eu.autogex.algorithms;

import org.eu.autogex.models.DFA;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class MinimizerTest {

    @Test
    void testMinimizeRedundantDFA() {
        // States q1 and q2 are completely equivalent.
        // State q5 is unreachable.
        DFA redundantDfa = new DFA.Builder()
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
        assertEquals(3, minimalDfa.getStates().size(), "The minimal DFA must have exactly 3 states");

        // Verify that the languages match
        String[] testStrings = {"a", "b", "abbb", "aa", "bbaa", "ababa", "", "bbbb"};
        for (String s : testStrings) {
            assertEquals(redundantDfa.accepts(s), minimalDfa.accepts(s),
                    "Both DFAs must behave identically for the string: " + s);
        }
    }

    @Test
    void testUtilityClassConstructorThrowsException() throws Exception {
        Constructor<Minimizer> constructor = Minimizer.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }
}