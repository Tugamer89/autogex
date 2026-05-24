package org.eu.autogex.export;

import java.util.Set;
import org.eu.autogex.core.Automaton;
import org.eu.autogex.core.State;
import org.eu.autogex.core.Transition;

/**
 * Utility class for exporting automata to the Mermaid.js stateDiagram-v2 format. This enables
 * native visual rendering within GitHub Markdown and other compatible platforms.
 */
public class MermaidExporter {

    private static final String EPSILON_LABEL = "ε";

    private MermaidExporter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Exports a DFA to a Mermaid format string.
     *
     * @param dfa The Deterministic Finite Automaton.
     * @return The Mermaid language representation.
     */
    /**
     * Exports an automaton to a Mermaid format string. Epsilon transitions (null keys) are
     * represented with the 'ε' symbol.
     *
     * @param automaton The Automaton (DFA, NFA, ENFA).
     * @return The Mermaid language representation.
     */
    public static String toMermaid(Automaton automaton) {
        StringBuilder sb =
                buildHeader(
                        automaton.getInitialState(),
                        automaton.getFinalStates(),
                        automaton.getStates());

        for (Transition transition : automaton.getTransitions()) {
            String sourceId = sanitizeId(transition.from());
            String label =
                    transition.symbol() == null ? EPSILON_LABEL : transition.symbol().toString();

            appendTransition(sb, sourceId, label, sanitizeId(transition.to()));
        }

        return sb.toString();
    }

    // --- Private Helper Methods ---

    private static StringBuilder buildHeader(
            State initialState, Set<State> finalStates, Set<State> allStates) {
        StringBuilder sb = new StringBuilder();
        sb.append("stateDiagram-v2\n");
        sb.append("    direction LR\n");

        // Declare state aliases to safely handle spaces or special characters in state names
        for (State s : allStates) {
            sb.append("    state \"")
                    .append(escapeMermaidString(s.getName()))
                    .append("\" as ")
                    .append(sanitizeId(s))
                    .append("\n");
        }

        // Define initial state entry point
        if (initialState != null) {
            sb.append("    [*] --> ").append(sanitizeId(initialState)).append("\n");
        }

        // Define final states exit points (rendered natively as transitions to [*])
        for (State f : finalStates) {
            sb.append("    ").append(sanitizeId(f)).append(" --> [*]\n");
        }

        return sb;
    }

    private static void appendTransition(StringBuilder sb, String from, String label, String to) {
        sb.append("    ")
                .append(from)
                .append(" --> ")
                .append(to)
                .append(" : ")
                .append(escapeMermaidString(label))
                .append("\n");
    }

    private static String escapeMermaidString(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /** Sanitizes the state name into a valid, safe identifier for Mermaid syntax. */
    private static String sanitizeId(State state) {
        // Replaces any non-alphanumeric character with an underscore to prevent syntax errors
        return "s_" + state.getName().replaceAll("[^a-zA-Z0-9]", "_");
    }
}
