package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.State;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NFATest {

    @Test
    void testNFAAcceptsPenultimateZero() {
        // Esempio 2.1.6
        // Linguaggio: stringhe su {0,1} in cui il PENULTIMO simbolo è 0.
        NFA nfa = new NFA.Builder()
                .addState("q0", false)
                .addState("q1", false)
                .addState("q2", true)
                .setInitialState("q0")
                // Transizioni da q0
                .addTransition("q0", '1', "q0")
                .addTransition("q0", '0', "q0")
                .addTransition("q0", '0', "q1")
                // Transizioni da q1
                .addTransition("q1", '0', "q2")
                .addTransition("q1", '1', "q2")
                .build();

        // ACCETTATE
        assertTrue(nfa.accepts("00"), "Accetta '00' (penultimo è 0)");
        assertTrue(nfa.accepts("01"), "Accetta '01'");
        assertTrue(nfa.accepts("10101"), "Accetta '10101'");
        assertTrue(nfa.accepts("111100"), "Accetta '111100'");

        // RIFIUTATE
        assertFalse(nfa.accepts("0"), "Rifiuta '0' (troppo corta)");
        assertFalse(nfa.accepts("10"), "Rifiuta '10' (penultimo è 1)");
        assertFalse(nfa.accepts("111"), "Rifiuta '111'");
        assertFalse(nfa.accepts(""), "Rifiuta stringa vuota");
    }

    @Test
    void testNFAGetters() {
        // Testa i metodi getter
        NFA nfa = new NFA.Builder()
                .addState("q0", true)
                .setInitialState("q0")
                .build();

        Set<State> states = nfa.getStates();
        assertEquals(1, states.size(), "Dovrebbe esserci esattamente 1 stato");
        
        State initial = nfa.getInitialState();
        assertNotNull(initial);
        assertEquals("q0", initial.getName(), "Lo stato iniziale deve essere q0");
        
        Set<State> finalStates = nfa.getFinalStates();
        assertEquals(1, finalStates.size(), "Dovrebbe esserci un solo stato finale");
        assertTrue(finalStates.iterator().next().isFinal(), "Lo stato in finalStates deve essere finale");
    }

    @Test
    void testBuilderThrowsIllegalArgumentExceptionForMissingState() {
        // Testa che venga lanciata un'eccezione se si aggiunge una transizione con stati non esistenti
        NFA.Builder builder = new NFA.Builder().addState("q0", false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addTransition("q0", 'a', "qX"); // "qX" non è mai stato aggiunto
        });

        assertTrue(exception.getMessage().contains("Stato non trovato"), "Il messaggio di errore deve indicare lo stato mancante");
    }

    @Test
    void testBuilderThrowsIllegalStateExceptionForMissingInitialState() {
        // Testa che non si possa costruire un NFA senza aver impostato lo stato iniziale
        NFA.Builder builder = new NFA.Builder().addState("q0", false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

        assertTrue(exception.getMessage().contains("stato iniziale"), "Il messaggio di errore deve menzionare lo stato iniziale");
    }
}