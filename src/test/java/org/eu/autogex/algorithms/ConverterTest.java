package org.eu.autogex.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConverterTest {

    @ParameterizedTest(name = "Testing ENFA to NFA conversion with input: ''{0}''")
    @ValueSource(strings = {"", "a", "b", "ab", "aaabbb", "ba", "aba", "c"})
    void testENFAToNFA(String input) {
        // ENFA accepting a*b*
        ENFA enfa =
                new ENFA.Builder()
                        .addState("q0", false)
                        .addState("q1", true)
                        .setInitialState("q0")
                        .addTransition("q0", 'a', "q0")
                        .addEpsilonTransition("q0", "q1")
                        .addTransition("q1", 'b', "q1")
                        .build();

        NFA nfa = Converter.enfaToNfa(enfa);

        assertEquals(
                enfa.accepts(input),
                nfa.accepts(input),
                "The converted NFA must yield the same result as the ENFA");

        // Verify that the NFA does not contain ε-transitions (null)
        for (var transitions : nfa.getTransitionTable().values()) {
            assertFalse(
                    transitions.containsKey(null), "An NFA must not contain ε-transitions (null)");
        }
    }

    @ParameterizedTest(name = "Testing NFA to DFA conversion with input: ''{0}''")
    @ValueSource(strings = {"01", "0001", "1101", "10101", "0", "1", "010", "111"})
    void testNFAToDFA(String input) {
        // NFA accepting strings ending with "01"
        NFA nfa =
                new NFA.Builder()
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

        assertEquals(
                nfa.accepts(input),
                dfa.accepts(input),
                "The converted DFA must yield the same result as the NFA");
    }

    @ParameterizedTest(name = "Testing ENFA to DFA full conversion with input: ''{0}''")
    @ValueSource(strings = {"", "a", "b", "ab", "aaabbb", "ba", "aba", "c"})
    void testENFAToDFA(String input) {
        // Complete chain: ENFA -> (NFA) -> DFA
        ENFA enfa =
                new ENFA.Builder()
                        .addState("q0", false)
                        .addState("q1", true)
                        .setInitialState("q0")
                        .addTransition("q0", 'a', "q0")
                        .addEpsilonTransition("q0", "q1")
                        .addTransition("q1", 'b', "q1")
                        .build();

        DFA dfa = Converter.enfaToDfa(enfa);

        assertEquals(
                enfa.accepts(input),
                dfa.accepts(input),
                "The DFA (from ENFA) must yield the same result as the original ENFA");
    }

    @Test
    void testUtilityClassConstructorThrowsException() throws Exception {
        Constructor<Converter> constructor = Converter.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        InvocationTargetException exception =
                assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }
}
