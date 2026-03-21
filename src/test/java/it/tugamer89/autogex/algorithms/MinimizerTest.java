package it.tugamer89.autogex.algorithms;

import it.tugamer89.autogex.models.DFA;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class MinimizerTest {

    @Test
    void testMinimizeRedundantDFA() {
        // Gli stati q1 e q2 sono totalmente equivalenti.
        // Lo stato q5 è irraggiungibile.
        DFA redundantDfa = new DFA.Builder()
                .addState("q0", false)
                .addState("q1", true)
                .addState("q2", true)
                .addState("q3", false)
                .addState("q5", true)
                .setInitialState("q0")
                
                // Transizioni da q0 vanno verso q1 e q2
                .addTransition("q0", 'a', "q1")
                .addTransition("q0", 'b', "q2")
                
                // q1 e q2 vanno entrambi in q3 con 'a' e restano in se stessi con 'b'
                .addTransition("q1", 'a', "q3")
                .addTransition("q1", 'b', "q1")
                .addTransition("q2", 'a', "q3")
                .addTransition("q2", 'b', "q2")
                
                // q3 torna a q0
                .addTransition("q3", 'a', "q0")
                .addTransition("q3", 'b', "q0")
                .build();

        assertEquals(5, redundantDfa.getStates().size());

        DFA minimalDfa = Minimizer.minimize(redundantDfa);

        // Totale stati attesi: q0_new, q1_q2_new, q3_new -> 3 stati!
        assertEquals(3, minimalDfa.getStates().size(), "Il DFA minimo deve avere esattamente 3 stati");

        // Verifichiamo che i linguaggi coincidano
        String[] testStrings = {"a", "b", "abbb", "aa", "bbaa", "ababa", "", "bbbb"};
        for (String s : testStrings) {
            assertEquals(redundantDfa.accepts(s), minimalDfa.accepts(s),
                    "I due DFA devono comportarsi in modo identico per la stringa: " + s);
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