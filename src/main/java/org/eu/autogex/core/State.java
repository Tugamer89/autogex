package org.eu.autogex.core;

import java.util.Objects;

/**
 * Represents a state (q) within an automaton.
 */
public class State {
    private final String name;
    private final boolean isFinal;

    /**
     * Constructs a new State.
     *
     * @param name    The unique name of the state.
     * @param isFinal True if the state is an accepting (final) state.
     */
    public State(String name, boolean isFinal) {
        this.name = name;
        this.isFinal = isFinal;
    }

    /**
     * Gets the name of the state.
     *
     * @return The state name.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if the state is a final (accepting) state.
     *
     * @return True if final, false otherwise.
     */
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return isFinal == state.isFinal && Objects.equals(name, state.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isFinal);
    }

    @Override
    public String toString() {
        return isFinal ? "*" + name : name;
    }
}