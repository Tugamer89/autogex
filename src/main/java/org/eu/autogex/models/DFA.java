package org.eu.autogex.models;

import java.util.*;
import org.eu.autogex.core.AbstractAutomaton;
import org.eu.autogex.core.AbstractAutomatonBuilder;
import org.eu.autogex.core.State;
import org.eu.autogex.trace.ExecutionStep;
import org.eu.autogex.trace.ExecutionTrace;

/** Deterministic Finite Automaton (DFA). */
public class DFA extends AbstractAutomaton {

    // Map: Source State -> (Map: Character -> Target State)
    private final Map<State, Map<Character, State>> transitionTable;
    private final Map<State, FastTransitions> fastTransitionTable;

    private DFA(Builder builder) {
        super(builder);
        this.transitionTable = Map.copyOf(builder.transitionTable);

        Map<State, FastTransitions> fastTable = new HashMap<>();
        for (Map.Entry<State, Map<Character, State>> entry : transitionTable.entrySet()) {
            fastTable.put(entry.getKey(), new FastTransitions(entry.getValue()));
        }
        this.fastTransitionTable = Map.copyOf(fastTable);
    }

    /**
     * Fast-path evaluation of an input string bypassing ExecutionTrace generation. Overrides the
     * default method to minimize memory allocation and maximize performance.
     *
     * @param input The string to be evaluated.
     * @return true if the string is accepted, false otherwise.
     */
    @Override
    public boolean accepts(String input) {
        validateExecutionInput(input);

        State currentState = initialState;

        int length = input.length();
        for (int i = 0; i < length; i++) {
            char symbol = input.charAt(i);
            FastTransitions stateTransitions = fastTransitionTable.get(currentState);

            if (stateTransitions == null) {
                return false;
            }

            currentState = stateTransitions.get(symbol);
            if (currentState == null) {
                return false;
            }
        }

        return finalStates.contains(currentState);
    }

    @Override
    public ExecutionTrace execute(String input) {
        validateExecutionInput(input);

        List<ExecutionStep> steps = new ArrayList<>();
        Set<State> currentStates = Set.of(initialState);

        // Initial setup step
        steps.add(new ExecutionStep(Collections.emptySet(), null, currentStates));

        int length = input.length();
        for (int i = 0; i < length; i++) {
            char symbol = input.charAt(i);
            State currentState = currentStates.iterator().next();
            FastTransitions stateTransitions = fastTransitionTable.get(currentState);

            Set<State> nextStates = Collections.emptySet();
            if (stateTransitions != null) {
                State target = stateTransitions.get(symbol);
                if (target != null) {
                    nextStates = Set.of(target);
                }
            }

            steps.add(new ExecutionStep(currentStates, symbol, nextStates));
            currentStates = nextStates;

            // If there is no defined transition, the string is rejected (Trap state)
            if (currentStates.isEmpty()) {
                break;
            }
        }

        boolean isAccepted =
                !currentStates.isEmpty() && finalStates.contains(currentStates.iterator().next());
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

    /** Builder pattern to construct the DFA fluently. */
    public static class Builder extends AbstractAutomatonBuilder<Builder, DFA> {

        private final Map<State, Map<Character, State>> transitionTable = new HashMap<>();

        /** Default constructor for DFA Builder. */
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
         * @param symbol The character required to trigger the transition.
         * @param toName The name of the destination state.
         * @return The current builder instance.
         */
        public Builder addTransition(String fromName, char symbol, String toName) {
            State[] transitionStates = getTransitionStatesOrThrow(fromName, toName);
            transitionTable
                    .computeIfAbsent(transitionStates[0], k -> new HashMap<>())
                    .put(symbol, transitionStates[1]);
            return self();
        }

        @Override
        public DFA build() {
            validate();
            return new DFA(this);
        }
    }

    /**
     * Array-backed transition table to avoid implicit char boxing in the fast-path evaluation loop.
     */
    private static final class FastTransitions {
        private final char[] symbols;
        private final State[] targetStates;

        FastTransitions(Map<Character, State> transitions) {
            int size = transitions.size();
            this.symbols = new char[size];
            this.targetStates = new State[size];
            int i = 0;
            for (Map.Entry<Character, State> entry : transitions.entrySet()) {
                this.symbols[i] = entry.getKey();
                this.targetStates[i] = entry.getValue();
                i++;
            }
        }

        State get(char symbol) {
            for (int i = 0; i < symbols.length; i++) {
                if (symbols[i] == symbol) {
                    return targetStates[i];
                }
            }
            return null;
        }
    }
}
