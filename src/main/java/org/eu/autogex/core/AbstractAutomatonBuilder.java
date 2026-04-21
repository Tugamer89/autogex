package org.eu.autogex.core;

import java.util.*;

/**
 * Abstract builder based on the Curiously Recurring Template Pattern (CRTP). Allows sharing state
 * creation logic among all specific Builders (DFA, NFA, ENFA) while maintaining fluid method
 * chaining.
 *
 * @param <B> The concrete Builder type (e.g., DFA.Builder)
 * @param <A> The Automaton type to build (e.g., DFA)
 */
public abstract class AbstractAutomatonBuilder<
        B extends AbstractAutomatonBuilder<B, A>, A extends Automaton> {

    protected final Map<String, State> states = new HashMap<>();
    protected final Set<State> finalStates = new HashSet<>();
    protected State initialState;

    /** Default constructor for the abstract builder. */
    protected AbstractAutomatonBuilder() {
        // Empty constructor since fields are initialized at declaration.
        // Required explicitly to maintain Javadoc and satisfy SonarQube rules.
    }

    /**
     * Abstract method that each concrete Builder must implement by returning 'this'. Ensures that
     * chaining returns the correct type.
     *
     * @return The current builder instance.
     */
    protected abstract B self();

    /**
     * Adds a new state to the automaton.
     *
     * @param name The name of the state.
     * @param isFinal True if the state is an accepting state.
     * @return The current builder instance.
     */
    public B addState(String name, boolean isFinal) {
        State state = new State(name, isFinal);
        states.put(name, state);
        if (isFinal) {
            finalStates.add(state);
        }
        return self();
    }

    /**
     * Sets the initial state of the automaton.
     *
     * @param name The name of an already added state.
     * @return The current builder instance.
     */
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
            throw new IllegalStateException(
                    "The initial state must be set before calling build().");
        }
    }

    /**
     * Validates and retrieves the states for a transition. Shared among all concrete builders to
     * prevent code duplication.
     *
     * @param fromName The name of the source state.
     * @param toName The name of the destination state.
     * @return An array containing [sourceState, targetState].
     * @throws IllegalArgumentException if either state does not exist.
     */
    protected State[] getTransitionStatesOrThrow(String fromName, String toName) {
        State from = states.get(fromName);
        State to = states.get(toName);
        if (from == null || to == null) {
            throw new IllegalArgumentException("State not found. Add it first using addState.");
        }
        return new State[] {from, to};
    }

    /**
     * Builds and returns the final automaton instance.
     *
     * @return The compiled automaton.
     */
    public abstract A build();

    /**
     * Gets the map of all registered states.
     *
     * @return The states map.
     */
    public Map<String, State> getStatesMap() {
        return states;
    }

    /**
     * Gets the set of all registered final states.
     *
     * @return The final states set.
     */
    public Set<State> getFinalStates() {
        return finalStates;
    }

    /**
     * Gets the registered initial state.
     *
     * @return The initial state.
     */
    public State getInitialState() {
        return initialState;
    }
}
