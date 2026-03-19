package it.tugamer89.autogex.core;

import java.util.Objects;

/**
 * Rappresenta uno stato (q) all'interno di un automa.
 */
public class State {
    private final String name;
    private final boolean isFinal;

    public State(String name, boolean isFinal) {
        this.name = name;
        this.isFinal = isFinal;
    }

    public String getName() {
        return name;
    }

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