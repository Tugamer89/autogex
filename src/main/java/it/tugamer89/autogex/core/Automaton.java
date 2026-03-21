package it.tugamer89.autogex.core;

import java.util.Set;

/**
 * Base interface for all types of Automata (DFA, NFA, ENFA).
 */
public interface Automaton {
    Set<State> getStates();
    State getInitialState();
    Set<State> getFinalStates();
    
    /**
     * The main method that checks whether the input string belongs
     * to the language recognized by the automaton L(M).
     *
     * @param input The string to be evaluated.
     * @return true if the string is accepted, false otherwise.
     */
    boolean accepts(String input);
}