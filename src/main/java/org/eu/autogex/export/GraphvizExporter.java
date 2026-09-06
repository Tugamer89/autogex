package org.eu.autogex.export;

import java.util.Set;
import java.util.stream.Collectors;
import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;

/**
 * Utility class for exporting automata to the Graphviz DOT language format. This allows for easy
 * visual representation of DFA, NFA, and ENFA models.
 */
public class GraphvizExporter extends AbstractExporter {

    private GraphvizExporter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Exports a DFA to a DOT format string.
     *
     * @param dfa The Deterministic Finite Automaton.
     * @return The DOT language representation.
     */
    public static String toDot(DFA dfa) {
        StringBuilder sb = buildDotHeader(dfa.getInitialState(), dfa.getFinalStates());

        traverseTransitions(
                dfa, (source, label, target) -> appendTransition(sb, source, label, target));

        return closeDot(sb);
    }

    /**
     * Exports an NFA to a DOT format string.
     *
     * @param nfa The Non-Deterministic Finite Automaton.
     * @return The DOT language representation.
     */
    public static String toDot(NFA nfa) {
        StringBuilder sb = buildDotHeader(nfa.getInitialState(), nfa.getFinalStates());

        traverseTransitions(
                nfa, (source, label, target) -> appendTransition(sb, source, label, target));

        return closeDot(sb);
    }

    /**
     * Exports an ENFA to a DOT format string. Epsilon transitions (null keys) are represented with
     * the 'ε' symbol.
     *
     * @param enfa The Epsilon-NFA.
     * @return The DOT language representation.
     */
    public static String toDot(ENFA enfa) {
        StringBuilder sb = buildDotHeader(enfa.getInitialState(), enfa.getFinalStates());

        traverseTransitions(
                enfa, (source, label, target) -> appendTransition(sb, source, label, target));

        return closeDot(sb);
    }

    // --- Private Helper Methods ---

    private static StringBuilder buildDotHeader(State initialState, Set<State> finalStates) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph Automaton {\n");
        sb.append("  rankdir=LR;\n"); // Left-to-Right orientation

        // Define final states appearance (Double Circle)
        if (!finalStates.isEmpty()) {
            String finalStatesList =
                    finalStates.stream()
                            .map(s -> "\"" + escapeDotString(s.getName()) + "\"")
                            .collect(Collectors.joining(" "));
            sb.append("  node [shape = doublecircle]; ").append(finalStatesList).append(";\n");
        }

        // Reset to default node appearance (Single Circle)
        sb.append("  node [shape = circle];\n");

        // Invisible entry node to point to the initial state
        sb.append("  __start0 [shape=none, label=\"\"];\n");
        if (initialState != null) {
            sb.append("  __start0 -> \"")
                    .append(escapeDotString(initialState.getName()))
                    .append("\";\n");
        }

        return sb;
    }

    private static void appendTransition(
            StringBuilder sb, State source, String label, State target) {
        sb.append("  \"")
                .append(escapeDotString(source.getName()))
                .append("\" -> \"")
                .append(escapeDotString(target.getName()))
                .append("\" [label=\"")
                .append(escapeDotString(label))
                .append("\"];\n");
    }

    private static String escapeDotString(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String closeDot(StringBuilder sb) {
        sb.append("}\n");
        return sb.toString();
    }
}
