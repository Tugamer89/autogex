package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.AbstractAutomaton;
import it.tugamer89.autogex.core.AbstractAutomatonBuilder;
import it.tugamer89.autogex.core.State;
import it.tugamer89.autogex.trace.ExecutionStep;
import it.tugamer89.autogex.trace.ExecutionTrace;

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
    public ExecutionTrace execute(String input) {
        List<ExecutionStep> steps = new ArrayList<>();
        Set<State> currentStates = Set.of(initialState);
        
        // Initial setup step
        steps.add(new ExecutionStep(Collections.emptySet(), null, currentStates));
        
        for (char symbol : input.toCharArray()) {
            State currentState = currentStates.iterator().next();
            Map<Character, State> stateTransitions = transitionTable.get(currentState);
            
            Set<State> nextStates = (stateTransitions != null && stateTransitions.containsKey(symbol))
                    ? Set.of(stateTransitions.get(symbol)) : Collections.emptySet();
            
            steps.add(new ExecutionStep(currentStates, symbol, nextStates));
            currentStates = nextStates;
            
            // If there is no defined transition, the string is rejected (Trap state)
            if (currentStates.isEmpty()) {
                break;
            }
        }
        
        boolean isAccepted = !currentStates.isEmpty() && finalStates.contains(currentStates.iterator().next());
        return new ExecutionTrace(input, steps, isAccepted);
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