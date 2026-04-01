package it.tugamer89.autogex.core;

import java.util.Set;

/**
 * Base interface for all types of Automata (DFA, NFA, ENFA).
 */
public interface Automaton {
    
    /**
     * Retrieves all the states within the automaton.
     *
     * @return A set containing all states.
     */
    Set<State> getStates();
    
    /**
     * Retrieves the starting state of the automaton.
     *
     * @return The initial state.
     */
    State getInitialState();
    
    /**
     * Retrieves all the accepting (final) states of the automaton.
     *
     * @return A set containing all final states.
     */
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