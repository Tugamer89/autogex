package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.AbstractAutomaton;
import it.tugamer89.autogex.core.AbstractAutomatonBuilder;
import it.tugamer89.autogex.core.State;

import java.util.*;

/**
 * Non-Deterministic Finite Automaton (NFA).
 */
public class NFA extends AbstractAutomaton {
    
    // Map: Source State -> (Map: Character -> Set of Target States)
    private final Map<State, Map<Character, Set<State>>> transitionTable;

    private NFA(Builder builder) {
        super(builder);
        this.transitionTable = Map.copyOf(builder.transitionTable);
    }

    @Override
    public boolean accepts(String input) {
        // The NFA can be in multiple states simultaneously
        Set<State> currentStates = new HashSet<>();
        currentStates.add(initialState);
        
        for (char symbol : input.toCharArray()) {
            Set<State> nextStates = new HashSet<>();
            
            for (State state : currentStates) {
                Map<Character, Set<State>> stateTransitions = transitionTable.get(state);
                if (stateTransitions != null && stateTransitions.containsKey(symbol)) {
                    nextStates.addAll(stateTransitions.get(symbol));
                }
            }
            
            currentStates = nextStates;
            
            // Optimization: if there are no more active states, the string is rejected
            if (currentStates.isEmpty()) {
                return false;
            }
        }
        
        // Accepts if at least one of the current active states is a final state
        return currentStates.stream().anyMatch(finalStates::contains);
    }

    public Map<State, Map<Character, Set<State>>> getTransitionTable() {
        return transitionTable;
    }

    /**
     * Builder pattern to construct the NFA fluently.
     */
    public static class Builder extends AbstractAutomatonBuilder<Builder, NFA> {
        
        private final Map<State, Map<Character, Set<State>>> transitionTable = new HashMap<>();

        @Override
        protected Builder self() {
            return this;
        }

        public Builder addTransition(String fromName, char symbol, String toName) {
            State from = states.get(fromName);
            State to = states.get(toName);
            
            if (from == null || to == null) {
                throw new IllegalArgumentException("State not found. Add it first using addState.");
            }

            transitionTable.computeIfAbsent(from, k -> new HashMap<>())
                           .computeIfAbsent(symbol, k -> new HashSet<>())
                           .add(to);
            return self();
        }

        @Override
        public NFA build() {
            validate();
            return new NFA(this);
        }
    }
}