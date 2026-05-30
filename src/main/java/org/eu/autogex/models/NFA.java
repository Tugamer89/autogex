package org.eu.autogex.models;

import java.util.*;
import org.eu.autogex.core.AbstractAutomaton;
import org.eu.autogex.core.AbstractAutomatonBuilder;
import org.eu.autogex.core.State;
import org.eu.autogex.trace.ExecutionStep;
import org.eu.autogex.trace.ExecutionTrace;

/** Non-Deterministic Finite Automaton (NFA). */
public class NFA extends AbstractAutomaton {

    // Map: Source State -> (Map: Character -> Set of Target States)
    private final Map<State, Map<Character, Set<State>>> transitionTable;

    private NFA(Builder builder) {
        super(builder);
        this.transitionTable = Map.copyOf(builder.transitionTable);
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
        Set<State> currentStates = Set.of(initialState);

        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);

            currentStates = computeNextStates(currentStates, symbol, transitionTable);

            if (currentStates.isEmpty()) {
                return false;
            }
        }

        return !Collections.disjoint(currentStates, finalStates);
    }

    @Override
    public ExecutionTrace execute(String input) {
        if (input.length() > ExecutionTrace.MAX_TRACE_INPUT_LENGTH) {
            throw new IllegalArgumentException(
                    "Input string too large for tracing (Security: DoS prevention).");
        }

        List<ExecutionStep> steps = new ArrayList<>();
        Set<State> currentStates = Set.of(initialState);

        // Initial setup step
        steps.add(new ExecutionStep(Collections.emptySet(), null, currentStates));

        for (char symbol : input.toCharArray()) {
            Set<State> nextStates = computeNextStates(currentStates, symbol, transitionTable);

            steps.add(new ExecutionStep(currentStates, symbol, nextStates));
            currentStates = nextStates;

            // Optimization: if there are no more active states, the string is rejected
            if (currentStates.isEmpty()) {
                break;
            }
        }

        boolean isAccepted = !Collections.disjoint(currentStates, finalStates);
        return new ExecutionTrace(input, steps, isAccepted);
    }

    /**
     * Retrieves the internal transition table of the NFA.
     *
     * @return The transition table.
     */
    public Map<State, Map<Character, Set<State>>> getTransitionTable() {
        return transitionTable;
    }

    /** Builder pattern to construct the NFA fluently. */
    public static class Builder extends AbstractAutomatonBuilder<Builder, NFA> {

        private final Map<State, Map<Character, Set<State>>> transitionTable = new HashMap<>();

        /** Default constructor for NFA Builder. */
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
         * @param symbol The character required to trigger the transition.
         * @param toName The name of the destination state.
         * @return The current builder instance.
         */
        public Builder addTransition(String fromName, char symbol, String toName) {
            State[] transitionStates = getTransitionStatesOrThrow(fromName, toName);
            transitionTable
                    .computeIfAbsent(transitionStates[0], k -> new HashMap<>())
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
