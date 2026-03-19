package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.State;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ENFATest {

    @Test
    void testENFAWithEpsilonTransitions() {
        // Automa che riconosce "a*b*" sfruttando la ε-transizione
        ENFA enfa = new ENFA.Builder()
                .addState("q0", true)
                .addState("q1", true)
                .setInitialState("q0")
                .addTransition("q0", 'a', "q0")
                .addEpsilonTransition("q0", "q1") // q0 --ε--> q1
                .addTransition("q1", 'b', "q1")
                .build();

        // ACCETTATE
        assertTrue(enfa.accepts(""), "Accetta stringa vuota (q0 finale)");
        assertTrue(enfa.accepts("a"), "Accetta 'a'");
        assertTrue(enfa.accepts("b"), "Accetta 'b' (salto ε in q1)");
        assertTrue(enfa.accepts("ab"), "Accetta 'ab'");
        assertTrue(enfa.accepts("aaaabbbb"), "Accetta 'aaaabbbb'");

        // RIFIUTATE
        assertFalse(enfa.accepts("ba"), "Rifiuta 'ba' (non si può tornare in dietro da q1)");
        assertFalse(enfa.accepts("aba"), "Rifiuta 'aba'");
        assertFalse(enfa.accepts("c"), "Rifiuta 'c'");
    }

    @Test
    void testENFAGetters() {
        // Testa i metodi getter
        ENFA enfa = new ENFA.Builder()
                .addState("q0", true)
                .setInitialState("q0")
                .build();

        Set<State> states = enfa.getStates();
        assertEquals(1, states.size(), "Dovrebbe esserci esattamente 1 stato");
        
        State initial = enfa.getInitialState();
        assertNotNull(initial);
        assertEquals("q0", initial.getName(), "Lo stato iniziale deve essere q0");
        
        Set<State> finalStates = enfa.getFinalStates();
        assertEquals(1, finalStates.size(), "Dovrebbe esserci un solo stato finale");
        assertTrue(finalStates.iterator().next().isFinal(), "Lo stato in finalStates deve essere finale");
    }

    @Test
    void testBuilderThrowsIllegalArgumentExceptionForMissingState() {
        // Testa che venga lanciata un'eccezione se si aggiunge una transizione con stati non esistenti
        ENFA.Builder builder = new ENFA.Builder().addState("q0", false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addEpsilonTransition("q0", "qX"); // "qX" non è mai stato aggiunto
        });

        assertTrue(exception.getMessage().contains("Stato non trovato"), "Il messaggio di errore deve indicare lo stato mancante");
    }

    @Test
    void testBuilderThrowsIllegalStateExceptionForMissingInitialState() {
        // Testa che non si possa costruire un ENFA senza aver impostato lo stato iniziale
        ENFA.Builder builder = new ENFA.Builder().addState("q0", false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

        assertTrue(exception.getMessage().contains("stato iniziale"), "Il messaggio di errore deve menzionare lo stato iniziale");
    }
}