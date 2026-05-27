package org.eu.autogex.export;

import java.util.Map;
import java.util.Set;
import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;

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
    public static String toMermaid(DFA dfa) {
        StringBuilder sb =
                buildHeader(dfa.getInitialState(), dfa.getFinalStates(), dfa.getStates());

        for (Map.Entry<State, Map<Character, State>> entry : dfa.getTransitionTable().entrySet()) {
            String sourceId = sanitizeId(entry.getKey());
            for (Map.Entry<Character, State> transition : entry.getValue().entrySet()) {
                appendTransition(
                        sb,
                        sourceId,
                        transition.getKey().toString(),
                        sanitizeId(transition.getValue()));
            }
        }

        return sb.toString();
    }

    /**
     * Exports an NFA to a Mermaid format string.
     *
     * @param nfa The Non-Deterministic Finite Automaton.
     * @return The Mermaid language representation.
     */
    public static String toMermaid(NFA nfa) {
        StringBuilder sb =
                buildHeader(nfa.getInitialState(), nfa.getFinalStates(), nfa.getStates());

        for (Map.Entry<State, Map<Character, Set<State>>> entry :
                nfa.getTransitionTable().entrySet()) {
            String sourceId = sanitizeId(entry.getKey());
            for (Map.Entry<Character, Set<State>> transition : entry.getValue().entrySet()) {
                for (State target : transition.getValue()) {
                    appendTransition(
                            sb, sourceId, transition.getKey().toString(), sanitizeId(target));
                }
            }
        }

        return sb.toString();
    }

    /**
     * Exports an ENFA to a Mermaid format string. Epsilon transitions (null keys) are represented
     * with the 'ε' symbol.
     *
     * @param enfa The Epsilon-NFA.
     * @return The Mermaid language representation.
     */
    public static String toMermaid(ENFA enfa) {
        StringBuilder sb =
                buildHeader(enfa.getInitialState(), enfa.getFinalStates(), enfa.getStates());

        for (Map.Entry<State, Map<Character, Set<State>>> entry :
                enfa.getTransitionTable().entrySet()) {
            String sourceId = sanitizeId(entry.getKey());
            for (Map.Entry<Character, Set<State>> transition : entry.getValue().entrySet()) {
                String label =
                        transition.getKey() == null
                                ? EPSILON_LABEL
                                : transition.getKey().toString();
                for (State target : transition.getValue()) {
                    appendTransition(sb, sourceId, label, sanitizeId(target));
                }
            }
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
        // Security concern: unescaped newlines can cause Mermaid syntax injection
        return text.replace("\r", "")
                .replace("\n", "\\n")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Sanitizes the state name into a valid, safe identifier for Mermaid syntax. Replaces any
     * non-alphanumeric character with an underscore to prevent syntax errors.
     */
    private static String sanitizeId(State state) {
        String name = state.getName();
        StringBuilder sb = new StringBuilder(name.length() + 2);
        sb.append("s_");
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }
}
