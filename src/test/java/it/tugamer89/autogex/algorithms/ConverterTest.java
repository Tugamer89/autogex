package it.tugamer89.autogex.algorithms;

import it.tugamer89.autogex.models.DFA;
import it.tugamer89.autogex.models.ENFA;
import it.tugamer89.autogex.models.NFA;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    @Test
    void testENFAToNFA() {
        // ENFA che accetta a*b*
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
                "L'NFA convertito deve dare lo stesso risultato dell'ENFA per: '" + s + "'");
        }
        
        for (var transitions : nfa.getTransitionTable().values()) {
            assertFalse(transitions.containsKey(null), "Un NFA non deve contenere ε-transizioni (null)");
        }
    }

    @Test
    void testNFAToDFA() {
        // NFA che accetta stringhe che terminano con "01"
        NFA nfa = new NFA.Builder()
                .addState("q0", false)
                .addState("q1", false)
                .addState("q2", true)
                .setInitialState("q0")
                .addTransition("q0", '0', "q0")
                .addTransition("q0", '1', "q0") // Loop iniziale
                .addTransition("q0", '0', "q1") // Inizio "01"
                .addTransition("q1", '1', "q2") // Fine "01"
                .build();

        DFA dfa = Converter.nfaToDfa(nfa);

        String[] testStrings = {"01", "0001", "1101", "10101", "0", "1", "010", "111"};
        for (String s : testStrings) {
            assertEquals(nfa.accepts(s), dfa.accepts(s), 
                "Il DFA convertito deve dare lo stesso risultato dell'NFA per: '" + s + "'");
        }
    }

    @Test
    void testENFAToDFA() {
        // Catena completa: ENFA -> (NFA) -> DFA
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
                "Il DFA (da ENFA) deve dare lo stesso risultato dell'ENFA originale per: '" + s + "'");
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