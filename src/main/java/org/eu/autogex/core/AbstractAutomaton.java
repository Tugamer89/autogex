package org.eu.autogex.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for all automata models. Centralizes the management of states and their
 * respective getters.
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

    @Override
    public Set<Transition> getTransitions() {
        return extractTransitions(getTransitionTableAsSetMap());
    }

    // Let subclasses provide a generalized transition table map for transition extraction.
    protected abstract Map<State, Map<Character, Set<State>>> getTransitionTableAsSetMap();

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
        Set<State> nextStates = new HashSet<>();
        for (State state : currentStates) {
            Map<Character, Set<State>> stateTransitions = transitionTable.get(state);
            if (stateTransitions != null && stateTransitions.containsKey(symbol)) {
                nextStates.addAll(stateTransitions.get(symbol));
            }
        }
        return nextStates;
    }

    /**
     * Helper method to extract transitions from a transition table mapping to multiple states.
     *
     * @param transitionTable The transition table.
     * @return The set of all transitions.
     */
    protected Set<Transition> extractTransitions(
            Map<State, Map<Character, Set<State>>> transitionTable) {
        Set<Transition> transitions = new HashSet<>();
        for (Map.Entry<State, Map<Character, Set<State>>> entry : transitionTable.entrySet()) {
            for (Map.Entry<Character, Set<State>> transition : entry.getValue().entrySet()) {
                for (State target : transition.getValue()) {
                    transitions.add(new Transition(entry.getKey(), transition.getKey(), target));
                }
            }
        }
        return transitions;
    }
}
