package it.tugamer89.autogex.export;

import it.tugamer89.autogex.core.State;
import it.tugamer89.autogex.models.DFA;
import it.tugamer89.autogex.models.ENFA;
import it.tugamer89.autogex.models.NFA;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for exporting automata to the Graphviz DOT language format.
 * This allows for easy visual representation of DFA, NFA, and ENFA models.
 */
public class GraphvizExporter {

    private static final String EPSILON_LABEL = "ε";

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

        for (Map.Entry<State, Map<Character, State>> entry : dfa.getTransitionTable().entrySet()) {
            State source = entry.getKey();
            for (Map.Entry<Character, State> transition : entry.getValue().entrySet()) {
                appendTransition(sb, source, transition.getKey().toString(), transition.getValue());
            }
        }

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

        for (Map.Entry<State, Map<Character, Set<State>>> entry : nfa.getTransitionTable().entrySet()) {
            State source = entry.getKey();
            for (Map.Entry<Character, Set<State>> transition : entry.getValue().entrySet()) {
                for (State target : transition.getValue()) {
                    appendTransition(sb, source, transition.getKey().toString(), target);
                }
            }
        }

        return closeDot(sb);
    }

    /**
     * Exports an ENFA to a DOT format string.
     * Epsilon transitions (null keys) are represented with the 'ε' symbol.
     *
     * @param enfa The Epsilon-NFA.
     * @return The DOT language representation.
     */
    public static String toDot(ENFA enfa) {
        StringBuilder sb = buildDotHeader(enfa.getInitialState(), enfa.getFinalStates());

        for (Map.Entry<State, Map<Character, Set<State>>> entry : enfa.getTransitionTable().entrySet()) {
            State source = entry.getKey();
            for (Map.Entry<Character, Set<State>> transition : entry.getValue().entrySet()) {
                String label = transition.getKey() == null ? EPSILON_LABEL : transition.getKey().toString();
                for (State target : transition.getValue()) {
                    appendTransition(sb, source, label, target);
                }
            }
        }

        return closeDot(sb);
    }

    // --- Private Helper Methods ---

    private static StringBuilder buildDotHeader(State initialState, Set<State> finalStates) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph Automaton {\n");
        sb.append("  rankdir=LR;\n"); // Left-to-Right orientation

        // Define final states appearance (Double Circle)
        if (!finalStates.isEmpty()) {
            String finalStatesList = finalStates.stream()
                    .map(s -> "\"" + s.getName() + "\"")
                    .collect(Collectors.joining(" "));
            sb.append("  node [shape = doublecircle]; ").append(finalStatesList).append(";\n");
        }

        // Reset to default node appearance (Single Circle)
        sb.append("  node [shape = circle];\n");

        // Invisible entry node to point to the initial state
        sb.append("  __start0 [shape=none, label=\"\"];\n");
        if (initialState != null) {
            sb.append("  __start0 -> \"").append(initialState.getName()).append("\";\n");
        }

        return sb;
    }

    private static void appendTransition(StringBuilder sb, State source, String label, State target) {
        sb.append("  \"").append(source.getName()).append("\" -> \"")
          .append(target.getName()).append("\" [label=\"").append(label).append("\"];\n");
    }

    private static String closeDot(StringBuilder sb) {
        sb.append("}\n");
        return sb.toString();
    }
}