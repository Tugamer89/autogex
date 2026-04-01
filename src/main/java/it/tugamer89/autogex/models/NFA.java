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
        Set<State> currentStates = new HashSet<>();
        currentStates.add(initialState);
        
        for (char symbol : input.toCharArray()) {
            currentStates = computeNextStates(currentStates, symbol, transitionTable);
            
            // Optimization: if there are no more active states, the string is rejected
            if (currentStates.isEmpty()) {
                return false;
            }
        }
        
        // Accepts if at least one of the current active states is a final state
        return currentStates.stream().anyMatch(finalStates::contains);
    }

    /**
     * Retrieves the internal transition table of the NFA.
     *
     * @return The transition table.
     */
    public Map<State, Map<Character, Set<State>>> getTransitionTable() {
        return transitionTable;
    }

    /**
     * Builder pattern to construct the NFA fluently.
     */
    public static class Builder extends AbstractAutomatonBuilder<Builder, NFA> {
        
        private final Map<State, Map<Character, Set<State>>> transitionTable = new HashMap<>();

        /**
         * Default constructor for NFA Builder.
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
         * Adds a non-deterministic transition between two states.
         *
         * @param fromName The name of the source state.
         * @param symbol   The character required to trigger the transition.
         * @param toName   The name of the destination state.
         * @return The current builder instance.
         */
        public Builder addTransition(String fromName, char symbol, String toName) {
            State[] transitionStates = getTransitionStatesOrThrow(fromName, toName);
            transitionTable.computeIfAbsent(transitionStates[0], k -> new HashMap<>())
                           .computeIfAbsent(symbol, k -> new HashSet<>())
                           .add(transitionStates[1]);
            return self();
        }

        @Override
        public NFA build() {
            validate();
            return new NFA(this);
        }
    }
}