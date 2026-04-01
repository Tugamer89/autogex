package it.tugamer89.autogex.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for all automata models.
 * Centralizes the management of states and their respective getters.
 */
public abstract class AbstractAutomaton implements Automaton {
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
     * Calculates the next active states for non-deterministic automata.
     * Protected and shared method to avoid duplicated code between NFA and ENFA (SonarQube).
     *
     * @param currentStates   The set of current states.
     * @param symbol          The symbol read from the input.
     * @param transitionTable The transition table of the child automaton.
     * @return The set of new reachable states.
     */
    protected Set<State> computeNextStates(Set<State> currentStates, char symbol, Map<State, Map<Character, Set<State>>> transitionTable) {
        Set<State> nextStates = new HashSet<>();
        for (State state : currentStates) {
            Map<Character, Set<State>> stateTransitions = transitionTable.get(state);
            if (stateTransitions != null && stateTransitions.containsKey(symbol)) {
                nextStates.addAll(stateTransitions.get(symbol));
            }
        }
        return nextStates;
    }
}