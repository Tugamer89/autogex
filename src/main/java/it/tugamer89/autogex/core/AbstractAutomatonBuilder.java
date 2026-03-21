package it.tugamer89.autogex.core;

import java.util.*;

/**
 * Abstract builder based on the Curiously Recurring Template Pattern (CRTP).
 * Allows sharing state creation logic among all specific Builders
 * (DFA, NFA, ENFA) while maintaining fluid method chaining.
 *
 * @param <B> The concrete Builder type (e.g., DFA.Builder)
 * @param <A> The Automaton type to build (e.g., DFA)
 */
public abstract class AbstractAutomatonBuilder<B extends AbstractAutomatonBuilder<B, A>, A extends Automaton> {
    
    protected final Map<String, State> states = new HashMap<>();
    protected final Set<State> finalStates = new HashSet<>();
    protected State initialState;

    /**
     * Abstract method that each concrete Builder must implement by returning 'this'.
     * Ensures that chaining returns the correct type.
     *
     * @return The current builder instance.
     */
    protected abstract B self();

    public B addState(String name, boolean isFinal) {
        State state = new State(name, isFinal);
        states.put(name, state);
        if (isFinal) {
            finalStates.add(state);
        }
        return self();
    }

    public B setInitialState(String name) {
        this.initialState = states.get(name);
        return self();
    }

    /**
     * Performs integrity checks common to all builders.
     *
     * @throws IllegalStateException if the builder configuration is invalid.
     */
    protected void validate() {
        if (initialState == null) {
            throw new IllegalStateException("The initial state must be set before calling build().");
        }
    }

    public abstract A build();

    public Map<String, State> getStatesMap() { return states; }
    public Set<State> getFinalStates() { return finalStates; }
    public State getInitialState() { return initialState; }
}