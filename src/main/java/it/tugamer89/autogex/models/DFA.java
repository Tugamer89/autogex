package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.AbstractAutomaton;
import it.tugamer89.autogex.core.AbstractAutomatonBuilder;
import it.tugamer89.autogex.core.State;

import java.util.*;

/**
 * Deterministic Finite Automaton (DFA).
 */
public class DFA extends AbstractAutomaton {
    
    // Map: Source State -> (Map: Character -> Target State)
    private final Map<State, Map<Character, State>> transitionTable;

    private DFA(Builder builder) {
        super(builder);
        this.transitionTable = Map.copyOf(builder.transitionTable);
    }

    @Override
    public boolean accepts(String input) {
        State currentState = initialState;
        
        for (char symbol : input.toCharArray()) {
            Map<Character, State> stateTransitions = transitionTable.get(currentState);
            
            // If there is no defined transition for this character, the string is rejected
            if (stateTransitions == null || !stateTransitions.containsKey(symbol)) {
                return false;
            }
            
            currentState = stateTransitions.get(symbol);
        }
        
        // The string is accepted if and only if we end up in a final state
        return finalStates.contains(currentState);
    }

    /**
     * Retrieves the internal transition table of the DFA.
     *
     * @return The transition table.
     */
    public Map<State, Map<Character, State>> getTransitionTable() {
        return transitionTable;
    }

    /**
     * Builder pattern to construct the DFA fluently.
     */
    public static class Builder extends AbstractAutomatonBuilder<Builder, DFA> {
        
        private final Map<State, Map<Character, State>> transitionTable = new HashMap<>();

        /**
         * Default constructor for DFA Builder.
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
         * Adds a transition between two states.
         *
         * @param fromName The name of the source state.
         * @param symbol   The character required to trigger the transition.
         * @param toName   The name of the destination state.
         * @return The current builder instance.
         */
        public Builder addTransition(String fromName, char symbol, String toName) {
            State[] transitionStates = getTransitionStatesOrThrow(fromName, toName);
            transitionTable.computeIfAbsent(transitionStates[0], k -> new HashMap<>()).put(symbol, transitionStates[1]);
            return self();
        }

        @Override
        public DFA build() {
            validate();
            return new DFA(this);
        }
    }
}