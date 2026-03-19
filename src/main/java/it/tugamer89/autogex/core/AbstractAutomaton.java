package it.tugamer89.autogex.core;

import java.util.Set;

/**
 * Classe base astratta per tutti i modelli di automi.
 * Centralizza la gestione degli stati e dei relativi getter.
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
}