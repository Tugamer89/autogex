package org.eu.autogex.trace;

import java.util.Set;
import java.util.stream.Collectors;
import org.eu.autogex.core.State;

/**
 * Represents a single evaluation step within an automaton's execution trace. Maintains immutability
 * utilizing Java Records.
 *
 * @param fromStates The set of active states before reading the symbol.
 * @param symbolRead The input symbol read during this step (null represents an epsilon transition).
 * @param toStates The set of active states reached after evaluating the symbol.
 */
public record ExecutionStep(Set<State> fromStates, Character symbolRead, Set<State> toStates) {

    /**
     * Formats the execution step into a highly readable mathematical string. Example: {q0, q1}
     * --[a]--> {q2}
     *
     * @return The formatted trace string.
     */
    @Override
    public String toString() {
        String from = formatStates(fromStates);
        String to = formatStates(toStates);
        String sym = symbolRead == null ? "ε" : symbolRead.toString();

        return String.format("%s --[%s]--> %s", from, sym, to);
    }

    private String formatStates(Set<State> states) {
        if (states == null || states.isEmpty()) {
            return "∅";
        }
        return "{"
                + states.stream().map(State::getName).sorted().collect(Collectors.joining(", "))
                + "}";
    }
}
