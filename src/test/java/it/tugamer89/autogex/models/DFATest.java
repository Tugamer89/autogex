package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.State;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DFATest {

    @Test
    void testDFAAcceptsStringsWithAtLeastOneZero() {
        // Automa dell'Esempio 2.1.3
        // Accetta le stringhe che contengono almeno un '0'
        DFA dfa = new DFA.Builder()
                .addState("q0", false)
                .addState("q1", true)   // q1 è l'unico stato finale
                .addState("q2", false)
                .setInitialState("q0")
                // Transizioni da q0
                .addTransition("q0", '0', "q1")
                .addTransition("q0", '1', "q2")
                // Transizioni da q1
                .addTransition("q1", '0', "q1")
                .addTransition("q1", '1', "q1")
                // Transizioni da q2
                .addTransition("q2", '0', "q1")
                .addTransition("q2", '1', "q0")
                .build();

        // Stringhe che contengono almeno uno zero
        assertTrue(dfa.accepts("0"), "Deve accettare un singolo 0");
        assertTrue(dfa.accepts("100"), "Deve accettare 100");
        assertTrue(dfa.accepts("11110"), "Deve accettare 11110");
        assertTrue(dfa.accepts("10101"), "Deve accettare 10101");

        // Stringhe senza zeri
        assertFalse(dfa.accepts("11"), "Non deve accettare 11");
        assertFalse(dfa.accepts("1"), "Non deve accettare 1");
        assertFalse(dfa.accepts("1111111"), "Non deve accettare solo 1");
        assertFalse(dfa.accepts(""), "Non deve accettare la stringa vuota");
    }

    @Test
    void testDFAGetters() {
        // Testa i metodi getter
        DFA dfa = new DFA.Builder()
                .addState("q0", false)
                .addState("q1", true)
                .setInitialState("q0")
                .addTransition("q0", 'a', "q1")
                .build();

        Set<State> states = dfa.getStates();
        assertEquals(2, states.size(), "Dovrebbero esserci esattamente 2 stati");

        State initial = dfa.getInitialState();
        assertNotNull(initial);
        assertEquals("q0", initial.getName(), "Lo stato iniziale deve essere q0");

        Set<State> finalStates = dfa.getFinalStates();
        assertEquals(1, finalStates.size(), "Dovrebbe esserci un solo stato finale");
        assertTrue(finalStates.iterator().next().isFinal(), "Lo stato in finalStates deve essere finale");
    }

    @Test
    void testBuilderThrowsIllegalArgumentExceptionForMissingState() {
        // Testa che venga lanciata un'eccezione se si aggiunge una transizione con stati non esistenti
        DFA.Builder builder = new DFA.Builder().addState("q0", false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addTransition("q0", 'a', "q1"); // "q1" non è mai stato aggiunto
        });

        assertTrue(exception.getMessage().contains("Stato non trovato"), "Il messaggio di errore deve indicare lo stato mancante");
    }

    @Test
    void testBuilderThrowsIllegalStateExceptionForMissingInitialState() {
        // Testa che non si possa costruire un DFA senza aver impostato lo stato iniziale
        DFA.Builder builder = new DFA.Builder()
                .addState("q0", false)
                .addState("q1", true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

        assertTrue(exception.getMessage().contains("stato iniziale"), "Il messaggio di errore deve menzionare lo stato iniziale");
    }
}