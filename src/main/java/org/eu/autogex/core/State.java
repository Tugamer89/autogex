package org.eu.autogex.core;

/** Represents a state (q) within an automaton. */
public class State {
    private final String name;
    private final boolean isFinal;
    private final int cachedHashCode;

    /**
     * Constructs a new State.
     *
     * @param name The unique name of the state.
     * @param isFinal True if the state is an accepting (final) state.
     */
    public State(String name, boolean isFinal) {
        this.name = name;
        this.isFinal = isFinal;

        // Cache hash code to speed up frequent map/set lookups during transformations.
        // Based on Objects.hash/Arrays.hashCode simplified for these two fields.
        int result = 1;
        result = 31 * result + (name == null ? 0 : name.hashCode());
        result = 31 * result + (isFinal ? 1231 : 1237);
        this.cachedHashCode = result;
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

        if (this.isFinal != state.isFinal) return false;
        if (this.name == null) return state.name == null;
        return this.name.equals(state.name);
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    @Override
    public String toString() {
        return isFinal ? "*" + name : name;
    }
}
