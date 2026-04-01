package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.AbstractAutomaton;
import it.tugamer89.autogex.core.AbstractAutomatonBuilder;
import it.tugamer89.autogex.core.State;

import java.util.*;

/**
 * Non-Deterministic Finite Automaton with Epsilon Transitions (ε-NFA).
 */
public class ENFA extends AbstractAutomaton {
    
    // The 'null' character is used to represent an ε-transition
    private final Map<State, Map<Character, Set<State>>> transitionTable;

    private ENFA(Builder builder) {
        super(builder);
        this.transitionTable = Map.copyOf(builder.transitionTable);
    }

    /**
     * Computes the ε-closure of a set of states.
     * (All states reachable without consuming any input).
     *
     * @param startStates The initial set of states.
     * @return The ε-closure set of states.
     */
    public Set<State> epsilonClosure(Set<State> startStates) {
        Set<State> closure = new HashSet<>(startStates);
        Queue<State> queue = new LinkedList<>(startStates);
        
        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            Map<Character, Set<State>> stateTransitions = transitionTable.get(currentState);
            
            // Look for transitions associated with null (ε)
            if (stateTransitions != null && stateTransitions.containsKey(null)) {
                for (State nextState : stateTransitions.get(null)) {
                    // If not visited yet, add it to the closure and to the queue
                    if (closure.add(nextState)) {
                        queue.add(nextState);
                    }
                }
            }
        }
        return closure;
    }

    @Override
    public boolean accepts(String input) {
        // Start from the ε-closure of the initial state
        Set<State> currentStates = epsilonClosure(Set.of(initialState));
        
        for (char symbol : input.toCharArray()) {
            Set<State> nextStates = new HashSet<>();
            
            for (State state : currentStates) {
                Map<Character, Set<State>> stateTransitions = transitionTable.get(state);
                if (stateTransitions != null && stateTransitions.containsKey(symbol)) {
                    nextStates.addAll(stateTransitions.get(symbol));
                }
            }
            
            // After reading the symbol, expand with the ε-closure
            currentStates = epsilonClosure(nextStates);
            
            if (currentStates.isEmpty()) {
                return false;
            }
        }
        
        return currentStates.stream().anyMatch(finalStates::contains);
    }

    /**
     * Retrieves the internal transition table of the ENFA.
     *
     * @return The transition table.
     */
    public Map<State, Map<Character, Set<State>>> getTransitionTable() {
        return transitionTable;
    }

    /**
     * Builder pattern to construct the ENFA fluently.
     */
    public static class Builder extends AbstractAutomatonBuilder<Builder, ENFA> {
        
        private final Map<State, Map<Character, Set<State>>> transitionTable = new HashMap<>();

        /**
         * Default constructor for ENFA Builder.
         */
        public Builder() {
            // Empty constructor since fields are initialized at declaration.
            // Required explicitly to maintain Javadoc and satisfy SonarQube rules.
        }

        @Override
        protected Builder self() {
            return this;
        }

        /**
         * Adds a transition (standard or epsilon) between two states.
         *
         * @param fromName The name of the source state.
         * @param symbol   The character required for the transition (null for epsilon).
         * @param toName   The name of the destination state.
         * @return The current builder instance.
         */
        public Builder addTransition(String fromName, Character symbol, String toName) {
            State from = states.get(fromName);
            State to = states.get(toName);
            
            if (from == null || to == null) {
                throw new IllegalArgumentException("State not found. Add it first using addState.");
            }

            transitionTable.computeIfAbsent(from, k -> new HashMap<>())
                           .computeIfAbsent(symbol, k -> new HashSet<>())
                           .add(to);
            return this;
        }

        /**
         * Utility method to make silent transitions more readable.
         *
         * @param fromName The name of the source state.
         * @param toName   The name of the destination state.
         * @return The current builder instance.
         */
        public Builder addEpsilonTransition(String fromName, String toName) {
            return addTransition(fromName, null, toName);
        }

        @Override
        public ENFA build() {
            validate();
            return new ENFA(this);
        }
    }
}