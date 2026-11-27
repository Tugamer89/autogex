package org.eu.autogex.io;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.eu.autogex.core.Automaton;
import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;

/**
 * Utility class for serializing finite state automata ({@link DFA}, {@link NFA}, and {@link ENFA})
 * into a canonical, deterministic JSON representation with zero external dependencies.
 *
 * <h2>Canonical JSON Schema</h2>
 *
 * <p>Every serialized automaton is represented as a UTF-8 JSON object conforming to RFC 8259 with
 * deterministic ordering across all collections:
 *
 * <ul>
 *   <li>{@code "type"} (string): the concrete automaton model ({@code "DFA"}, {@code "NFA"}, or
 *       {@code "ENFA"}).
 *   <li>{@code "alphabet"} (array of single-character strings): all non-epsilon input symbols
 *       appearing in transitions, sorted in natural Unicode code-point order. Empty ({@code []})
 *       when the automaton defines no symbol transitions.
 *   <li>{@code "states"} (array of strings): all state identifiers in the automaton (including
 *       unreachable states), sorted lexicographically.
 *   <li>{@code "initialState"} (string): the identifier of the starting state.
 *   <li>{@code "acceptingStates"} (array of strings): all accepting (final) state identifiers,
 *       sorted lexicographically.
 *   <li>{@code "transitions"} (array of objects): all transitions sorted by {@code "from"}, then
 *       {@code "symbol"} (natural order, with {@code null} placed last), then {@code "to"}. Each
 *       entry contains:
 *       <ul>
 *         <li>{@code "from"} (string): source state identifier.
 *         <li>{@code "symbol"} (single-character string or {@code null}): consumed input symbol, or
 *             {@code null} for silent ε-transitions in an {@link ENFA}.
 *         <li>{@code "to"} (string): target state identifier.
 *       </ul>
 * </ul>
 *
 * <h2>Example Output</h2>
 *
 * <pre>{@code
 * {
 *   "type": "DFA",
 *   "alphabet": [
 *     "a",
 *     "b"
 *   ],
 *   "states": [
 *     "q0",
 *     "q1"
 *   ],
 *   "initialState": "q0",
 *   "acceptingStates": [
 *     "q1"
 *   ],
 *   "transitions": [
 *     {
 *       "from": "q0",
 *       "symbol": "a",
 *       "to": "q1"
 *     },
 *     {
 *       "from": "q1",
 *       "symbol": "b",
 *       "to": "q0"
 *     }
 *   ]
 * }
 * }</pre>
 */
public final class AutomatonSerializer {

    private static final Comparator<SerializedTransition> TRANSITION_COMPARATOR =
            Comparator.comparing(SerializedTransition::from)
                    .thenComparing(
                            SerializedTransition::symbol,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(SerializedTransition::to);

    private record SerializedTransition(String from, Character symbol, String to) {}

    private AutomatonSerializer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Serializes a finite automaton ({@link DFA}, {@link NFA}, or {@link ENFA}) into its canonical,
     * deterministic JSON string representation.
     *
     * @param automaton the finite automaton to serialize, must not be {@code null}
     * @return a deterministic, RFC 8259 compliant JSON string representing {@code automaton}
     * @throws NullPointerException if {@code automaton} is {@code null}
     * @throws IllegalArgumentException if {@code automaton} is not an instance of {@link DFA},
     *     {@link NFA}, or {@link ENFA}
     */
    public static String toJson(Automaton automaton) {
        Objects.requireNonNull(automaton, "Automaton cannot be null");

        String type;
        Set<Character> alphabet = new TreeSet<>();
        List<SerializedTransition> transitions = new ArrayList<>();

        switch (automaton) {
            case DFA dfa -> {
                type = "DFA";
                collectDfaTransitions(dfa, alphabet, transitions);
            }
            case NFA nfa -> {
                type = "NFA";
                collectNfaTransitions(nfa, alphabet, transitions);
            }
            case ENFA enfa -> {
                type = "ENFA";
                collectEnfaTransitions(enfa, alphabet, transitions);
            }
            default ->
                    throw new IllegalArgumentException(
                            "Unsupported automaton type: " + automaton.getClass().getName());
        }

        transitions.sort(TRANSITION_COMPARATOR);

        List<String> states = automaton.getStates().stream().map(State::getName).sorted().toList();

        List<String> acceptingStates =
                automaton.getFinalStates().stream().map(State::getName).sorted().toList();

        String initialState = automaton.getInitialState().getName();

        return renderJson(type, alphabet, states, initialState, acceptingStates, transitions);
    }

    private static void collectDfaTransitions(
            DFA dfa, Set<Character> alphabet, List<SerializedTransition> transitions) {
        for (Map.Entry<State, Map<Character, State>> entry : dfa.getTransitionTable().entrySet()) {
            String from = entry.getKey().getName();
            for (Map.Entry<Character, State> transition : entry.getValue().entrySet()) {
                char symbol = transition.getKey();
                alphabet.add(symbol);
                transitions.add(
                        new SerializedTransition(from, symbol, transition.getValue().getName()));
            }
        }
    }

    private static void collectNfaTransitions(
            NFA nfa, Set<Character> alphabet, List<SerializedTransition> transitions) {
        for (Map.Entry<State, Map<Character, Set<State>>> entry :
                nfa.getTransitionTable().entrySet()) {
            String from = entry.getKey().getName();
            for (Map.Entry<Character, Set<State>> transition : entry.getValue().entrySet()) {
                char symbol = transition.getKey();
                alphabet.add(symbol);
                for (State target : transition.getValue()) {
                    transitions.add(new SerializedTransition(from, symbol, target.getName()));
                }
            }
        }
    }

    private static void collectEnfaTransitions(
            ENFA enfa, Set<Character> alphabet, List<SerializedTransition> transitions) {
        for (Map.Entry<State, Map<Character, Set<State>>> entry :
                enfa.getTransitionTable().entrySet()) {
            String from = entry.getKey().getName();
            for (Map.Entry<Character, Set<State>> transition : entry.getValue().entrySet()) {
                Character symbol = transition.getKey();
                if (symbol != null) {
                    alphabet.add(symbol);
                }
                for (State target : transition.getValue()) {
                    transitions.add(new SerializedTransition(from, symbol, target.getName()));
                }
            }
        }
    }

    private static String renderJson(
            String type,
            Set<Character> alphabet,
            List<String> states,
            String initialState,
            List<String> acceptingStates,
            List<SerializedTransition> transitions) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"type\": \"").append(escapeJsonString(type)).append("\",\n");

        List<String> alphabetStrings = alphabet.stream().map(String::valueOf).toList();
        appendStringArray(sb, "alphabet", alphabetStrings);
        sb.append(",\n");

        appendStringArray(sb, "states", states);
        sb.append(",\n");

        sb.append("  \"initialState\": \"").append(escapeJsonString(initialState)).append("\",\n");

        appendStringArray(sb, "acceptingStates", acceptingStates);
        sb.append(",\n");

        appendTransitionsArray(sb, transitions);
        sb.append("\n}\n");
        return sb.toString();
    }

    private static void appendStringArray(StringBuilder sb, String key, List<String> values) {
        sb.append("  \"").append(key).append("\": ");
        if (values.isEmpty()) {
            sb.append("[]");
            return;
        }
        sb.append("[\n");
        for (int i = 0; i < values.size(); i++) {
            sb.append("    \"").append(escapeJsonString(values.get(i))).append('"');
            if (i < values.size() - 1) {
                sb.append(',');
            }
            sb.append('\n');
        }
        sb.append("  ]");
    }

    private static void appendTransitionsArray(
            StringBuilder sb, List<SerializedTransition> transitions) {
        sb.append("  \"transitions\": ");
        if (transitions.isEmpty()) {
            sb.append("[]");
            return;
        }
        sb.append("[\n");
        for (int i = 0; i < transitions.size(); i++) {
            SerializedTransition t = transitions.get(i);
            sb.append("    {\n");
            sb.append("      \"from\": \"").append(escapeJsonString(t.from())).append("\",\n");
            if (t.symbol() == null) {
                sb.append("      \"symbol\": null,\n");
            } else {
                sb.append("      \"symbol\": \"")
                        .append(escapeJsonString(t.symbol().toString()))
                        .append("\",\n");
            }
            sb.append("      \"to\": \"").append(escapeJsonString(t.to())).append("\"\n");
            sb.append("    }");
            if (i < transitions.size() - 1) {
                sb.append(',');
            }
            sb.append('\n');
        }
        sb.append("  ]");
    }

    /** Escapes a string value according to RFC 8259 and neutralizes HTML-sensitive characters. */
    private static String escapeJsonString(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<' -> sb.append("\\u003c");
                case '>' -> sb.append("\\u003e");
                case '&' -> sb.append("\\u0026");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
