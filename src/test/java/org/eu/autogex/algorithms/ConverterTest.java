package org.eu.autogex.algorithms;

import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    @Test
    void testENFAToNFA() {
        // ENFA accepting a*b*
        ENFA enfa = new ENFA.Builder()
                .addState("q0", false)
                .addState("q1", true)
                .setInitialState("q0")
                .addTransition("q0", 'a', "q0")
                .addEpsilonTransition("q0", "q1")
                .addTransition("q1", 'b', "q1")
                .build();

        NFA nfa = Converter.enfaToNfa(enfa);

        String[] testStrings = {"", "a", "b", "ab", "aaabbb", "ba", "aba", "c"};
        for (String s : testStrings) {
            assertEquals(enfa.accepts(s), nfa.accepts(s), 
                "The converted NFA must yield the same result as the ENFA for: '" + s + "'");
        }
        
        for (var transitions : nfa.getTransitionTable().values()) {
            assertFalse(transitions.containsKey(null), "An NFA must not contain ε-transitions (null)");
        }
    }

    @Test
    void testNFAToDFA() {
        // NFA accepting strings ending with "01"
        NFA nfa = new NFA.Builder()
                .addState("q0", false)
                .addState("q1", false)
                .addState("q2", true)
                .setInitialState("q0")
                .addTransition("q0", '0', "q0")
                .addTransition("q0", '1', "q0") // Initial loop
                .addTransition("q0", '0', "q1") // Start of "01"
                .addTransition("q1", '1', "q2") // End of "01"
                .build();

        DFA dfa = Converter.nfaToDfa(nfa);

        String[] testStrings = {"01", "0001", "1101", "10101", "0", "1", "010", "111"};
        for (String s : testStrings) {
            assertEquals(nfa.accepts(s), dfa.accepts(s), 
                "The converted DFA must yield the same result as the NFA for: '" + s + "'");
        }
    }

    @Test
    void testENFAToDFA() {
        // Complete chain: ENFA -> (NFA) -> DFA
        ENFA enfa = new ENFA.Builder()
                .addState("q0", false)
                .addState("q1", true)
                .setInitialState("q0")
                .addTransition("q0", 'a', "q0")
                .addEpsilonTransition("q0", "q1")
                .addTransition("q1", 'b', "q1")
                .build();

        DFA dfa = Converter.enfaToDfa(enfa);

        String[] testStrings = {"", "a", "b", "ab", "aaabbb", "ba", "aba", "c"};
        for (String s : testStrings) {
            assertEquals(enfa.accepts(s), dfa.accepts(s), 
                "The DFA (from ENFA) must yield the same result as the original ENFA for: '" + s + "'");
        }
    }

    @Test
    void testUtilityClassConstructorThrowsException() throws Exception {
        Constructor<Converter> constructor = Converter.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }
}