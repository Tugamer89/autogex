package org.eu.autogex.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for all automata models. Centralizes the management of states and their
 * respective getters.
 */
public abstract class AbstractAutomaton implements Automaton {

    /** Maximum allowed input length for trace execution to prevent memory DoS. */
    protected static final int MAX_EXECUTION_STEPS = 10000;

    protected final Set<State> states;
    protected final State initialState;
    protected final Set<State> finalStates;

    protected AbstractAutomaton(AbstractAutomatonBuilder<?, ?> builder) {
        this.states = Set.copyOf(builder.getStatesMap().values());
        this.initialState = builder.getInitialState();
        this.finalStates = Set.copyOf(builder.getFinalStates());
    }

    @Override
    public Set<State> getStates() {
        return states;
    }

    @Override
    public State getInitialState() {
        return initialState;
    }

    @Override
    public Set<State> getFinalStates() {
        return finalStates;
    }

    /**
     * Calculates the next active states for non-deterministic automata. Protected and shared method
     * to avoid duplicated code between NFA and ENFA (SonarQube).
     *
     * @param currentStates The set of current states.
     * @param symbol The symbol read from the input.
     * @param transitionTable The transition table of the child automaton.
     * @return The set of new reachable states.
     */
    protected Set<State> computeNextStates(
            Set<State> currentStates,
            char symbol,
            Map<State, Map<Character, Set<State>>> transitionTable) {
        Set<State> nextStates = null;

        for (State state : currentStates) {
            Map<Character, Set<State>> stateTransitions = transitionTable.get(state);
            if (stateTransitions != null) {
                Set<State> targets = stateTransitions.get(symbol);
                if (targets != null && !targets.isEmpty()) {
                    if (nextStates == null) {
                        nextStates = new HashSet<>(targets);
                    } else {
                        nextStates.addAll(targets);
                    }
                }
            }
        }
        return nextStates != null ? nextStates : Collections.emptySet();
    }

    /**
     * Validates the input string for trace execution to prevent memory DoS.
     *
     * @param input The string to validate.
     */
    protected void validateExecutionInput(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input string cannot be null.");
        }
        if (input.length() > MAX_EXECUTION_STEPS) {
            throw new IllegalArgumentException(
                    "Input string exceeds maximum allowed length for trace execution (Security: DoS prevention).");
        }
    }
}
