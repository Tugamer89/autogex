package it.tugamer89.autogex.core;

/**
 * Represents a single transition from one state to another by reading a symbol.
 *
 * @param from   The starting state of the transition.
 * @param symbol The symbol read during the transition (null indicates an epsilon transition).
 * @param to     The destination state.
 */
public record Transition(State from, Character symbol, State to) {

    /**
     * Checks if this is a silent transition.
     * We use 'null' to represent epsilon (ε).
     *
     * @return true if it is an epsilon transition, false otherwise.
     */
    public boolean isEpsilon() {
        return symbol == null;
    }

    @Override
    public String toString() {
        String symStr = isEpsilon() ? "ε" : symbol.toString();
        return from.getName() + " --" + symStr + "--> " + to.getName();
    }
}