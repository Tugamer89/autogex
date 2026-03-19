package it.tugamer89.autogex.core;

import java.util.Set;

/**
 * Interfaccia base per tutti i tipi di Automi (DFA, NFA, ENFA).
 */
public interface Automaton {
    Set<State> getStates();
    State getInitialState();
    Set<State> getFinalStates();
    
    /**
     * Il metodo principale che verifica se la stringa in input appartiene
     * al linguaggio riconosciuto dall'automa L(M).
     */
    boolean accepts(String input);
}