package org.eu.autogex.io;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
 * Utility class for serializing and deserializing finite state automata ({@link DFA}, {@link NFA},
 * and {@link ENFA}) using a canonical, deterministic JSON representation with zero external
 * dependencies.
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
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * DFA dfa = new Regex("(a|b)*abb").getAutomaton();
 * String json = AutomatonSerializer.toJson(dfa);
 *
 * Automaton restored = AutomatonSerializer.fromJson(json);
 * boolean accepted = restored.accepts("aabb"); // true
 * }</pre>
 */
public final class AutomatonSerializer {

    /** Maximum allowed JSON payload length in characters to prevent memory DoS. */
    private static final int MAX_JSON_LENGTH = 5_000_000;

    /** Maximum allowed JSON nesting depth to prevent StackOverflowError DoS. */
    private static final int MAX_NESTING_DEPTH = 32;

    /** Sentinel representing an explicit JSON {@code null} value inside parsed maps. */
    private static final Object JSON_NULL = new Object();

    private static final String KEY_TYPE = "type";
    private static final String KEY_ALPHABET = "alphabet";
    private static final String KEY_STATES = "states";
    private static final String KEY_INITIAL_STATE = "initialState";
    private static final String KEY_ACCEPTING_STATES = "acceptingStates";
    private static final String KEY_TRANSITIONS = "transitions";
    private static final String KEY_FROM = "from";
    private static final String KEY_SYMBOL = "symbol";
    private static final String KEY_TO = "to";

    private static final Set<String> ROOT_KEYS =
            Set.of(
                    KEY_TYPE,
                    KEY_ALPHABET,
                    KEY_STATES,
                    KEY_INITIAL_STATE,
                    KEY_ACCEPTING_STATES,
                    KEY_TRANSITIONS);

    private static final Set<String> TRANSITION_KEYS = Set.of(KEY_FROM, KEY_SYMBOL, KEY_TO);

    private static final Comparator<SerializedTransition> TRANSITION_COMPARATOR =
            Comparator.comparing(SerializedTransition::from)
                    .thenComparing(
                            SerializedTransition::symbol,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(SerializedTransition::to);

    private record SerializedTransition(String from, Character symbol, String to) {}

    private record StateSymbol(String state, char symbol) {}

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

    /**
     * Deserializes a finite automaton ({@link DFA}, {@link NFA}, or {@link ENFA}) from its
     * canonical JSON representation.
     *
     * <p>Reconstructs the target model exclusively through {@link DFA.Builder}, {@link
     * NFA.Builder}, or {@link ENFA.Builder} so all immutability guarantees and structural
     * invariants remain strictly enforced.
     *
     * @param json the JSON string representing a serialized automaton, must not be {@code null}
     * @return an immutable {@link DFA}, {@link NFA}, or {@link ENFA} instance
     * @throws NullPointerException if {@code json} is {@code null}
     * @throws IllegalArgumentException if {@code json} is malformed, exceeds safety limits, misses
     *     required fields, or violates automaton invariants
     */
    public static Automaton fromJson(String json) {
        Objects.requireNonNull(json, "JSON string cannot be null");
        if (json.length() > MAX_JSON_LENGTH) {
            throw new IllegalArgumentException(
                    "JSON payload exceeds maximum allowed length of "
                            + MAX_JSON_LENGTH
                            + " characters (Security: DoS prevention).");
        }
        if (json.isBlank()) {
            throw new IllegalArgumentException("JSON string cannot be blank");
        }

        Map<String, Object> root = new JsonParser(json).parseRootObject();
        validateAllowedKeys(root, ROOT_KEYS, "root object");

        String type = requireString(root, KEY_TYPE);
        Set<Character> alphabet = parseAlphabet(requireArray(root, KEY_ALPHABET));
        List<String> states = parseUniqueStringList(requireArray(root, KEY_STATES), KEY_STATES);
        if (states.isEmpty()) {
            throw new IllegalArgumentException("Field 'states' must contain at least one state");
        }

        Set<String> stateSet = new HashSet<>(states);
        String initialState = requireString(root, KEY_INITIAL_STATE);
        if (!stateSet.contains(initialState)) {
            throw new IllegalArgumentException(
                    "Initial state '" + initialState + "' is not declared in 'states'");
        }

        List<String> acceptingList =
                parseUniqueStringList(
                        requireArray(root, KEY_ACCEPTING_STATES), KEY_ACCEPTING_STATES);
        for (String accepting : acceptingList) {
            if (!stateSet.contains(accepting)) {
                throw new IllegalArgumentException(
                        "Accepting state '" + accepting + "' is not declared in 'states'");
            }
        }
        Set<String> acceptingSet = new HashSet<>(acceptingList);

        List<SerializedTransition> transitions =
                parseTransitions(requireArray(root, KEY_TRANSITIONS), alphabet, type);

        return buildAutomaton(type, states, initialState, acceptingSet, transitions);
    }

    private static Automaton buildAutomaton(
            String type,
            List<String> states,
            String initialState,
            Set<String> acceptingSet,
            List<SerializedTransition> transitions) {
        return switch (type) {
            case "DFA" -> buildDfa(states, initialState, acceptingSet, transitions);
            case "NFA" -> buildNfa(states, initialState, acceptingSet, transitions);
            case "ENFA" -> buildEnfa(states, initialState, acceptingSet, transitions);
            default -> throw new IllegalArgumentException("Unsupported automaton type: " + type);
        };
    }

    private static DFA buildDfa(
            List<String> states,
            String initialState,
            Set<String> acceptingSet,
            List<SerializedTransition> transitions) {
        DFA.Builder builder = new DFA.Builder();
        for (String state : states) {
            builder.addState(state, acceptingSet.contains(state));
        }
        builder.setInitialState(initialState);

        Set<StateSymbol> seen = new HashSet<>();
        for (SerializedTransition t : transitions) {
            char symbol = t.symbol();
            if (!seen.add(new StateSymbol(t.from(), symbol))) {
                throw new IllegalArgumentException(
                        "Duplicate transition for state '"
                                + t.from()
                                + "' on symbol '"
                                + symbol
                                + "' in DFA");
            }
            builder.addTransition(t.from(), symbol, t.to());
        }
        return builder.build();
    }

    private static NFA buildNfa(
            List<String> states,
            String initialState,
            Set<String> acceptingSet,
            List<SerializedTransition> transitions) {
        NFA.Builder builder = new NFA.Builder();
        for (String state : states) {
            builder.addState(state, acceptingSet.contains(state));
        }
        builder.setInitialState(initialState);

        for (SerializedTransition t : transitions) {
            builder.addTransition(t.from(), t.symbol(), t.to());
        }
        return builder.build();
    }

    private static ENFA buildEnfa(
            List<String> states,
            String initialState,
            Set<String> acceptingSet,
            List<SerializedTransition> transitions) {
        ENFA.Builder builder = new ENFA.Builder();
        for (String state : states) {
            builder.addState(state, acceptingSet.contains(state));
        }
        builder.setInitialState(initialState);

        for (SerializedTransition t : transitions) {
            if (t.symbol() == null) {
                builder.addEpsilonTransition(t.from(), t.to());
            } else {
                builder.addTransition(t.from(), t.symbol(), t.to());
            }
        }
        return builder.build();
    }

    private static void validateAllowedKeys(
            Map<String, Object> obj, Set<String> allowedKeys, String context) {
        for (String key : obj.keySet()) {
            if (!allowedKeys.contains(key)) {
                throw new IllegalArgumentException(
                        "Unknown JSON field '" + key + "' in " + context);
            }
        }
    }

    private static String requireString(Map<String, Object> obj, String key) {
        Object value = obj.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required JSON field: '" + key + "'");
        }
        if (!(value instanceof String str)) {
            throw new IllegalArgumentException("JSON field '" + key + "' must be a string");
        }
        return str;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> requireArray(Map<String, Object> obj, String key) {
        Object value = obj.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required JSON field: '" + key + "'");
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("JSON field '" + key + "' must be an array");
        }
        return (List<Object>) list;
    }

    private static Set<Character> parseAlphabet(List<Object> rawAlphabet) {
        Set<Character> alphabet = new HashSet<>();
        for (Object item : rawAlphabet) {
            if (!(item instanceof String str) || str.length() != 1) {
                throw new IllegalArgumentException(
                        "Entries in 'alphabet' must be single-character strings");
            }
            char symbol = str.charAt(0);
            if (!alphabet.add(symbol)) {
                throw new IllegalArgumentException(
                        "Duplicate symbol '" + symbol + "' in 'alphabet'");
            }
        }
        return alphabet;
    }

    private static List<String> parseUniqueStringList(List<Object> rawList, String fieldName) {
        List<String> result = new ArrayList<>(rawList.size());
        Set<String> seen = new HashSet<>();
        for (Object item : rawList) {
            if (!(item instanceof String str)) {
                throw new IllegalArgumentException(
                        "Entries in '" + fieldName + "' must be strings");
            }
            if (!seen.add(str)) {
                throw new IllegalArgumentException(
                        "Duplicate entry '" + str + "' in '" + fieldName + "'");
            }
            result.add(str);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<SerializedTransition> parseTransitions(
            List<Object> rawTransitions, Set<Character> alphabet, String type) {
        List<SerializedTransition> result = new ArrayList<>(rawTransitions.size());
        for (Object item : rawTransitions) {
            if (!(item instanceof Map<?, ?> rawMap)) {
                throw new IllegalArgumentException("Entries in 'transitions' must be JSON objects");
            }
            Map<String, Object> transitionObj = (Map<String, Object>) rawMap;
            validateAllowedKeys(transitionObj, TRANSITION_KEYS, "transition object");

            String from = requireString(transitionObj, KEY_FROM);
            String to = requireString(transitionObj, KEY_TO);
            Character symbol = parseTransitionSymbol(transitionObj, alphabet, type);

            result.add(new SerializedTransition(from, symbol, to));
        }
        return result;
    }

    private static Character parseTransitionSymbol(
            Map<String, Object> transitionObj, Set<Character> alphabet, String type) {
        Object rawSymbol = transitionObj.get(KEY_SYMBOL);
        if (rawSymbol == null) {
            throw new IllegalArgumentException(
                    "Missing required JSON field: 'symbol' in transition object");
        }
        if (rawSymbol == JSON_NULL) {
            if (!"ENFA".equals(type)) {
                throw new IllegalArgumentException(
                        "Epsilon transitions (symbol: null) are only supported for ENFA");
            }
            return null;
        }
        if (!(rawSymbol instanceof String str) || str.length() != 1) {
            throw new IllegalArgumentException(
                    "Transition 'symbol' must be a single-character string or null");
        }
        char symbol = str.charAt(0);
        if (!alphabet.contains(symbol)) {
            throw new IllegalArgumentException(
                    "Transition symbol '" + symbol + "' is not declared in 'alphabet'");
        }
        return symbol;
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
        appendStringArray(sb, KEY_ALPHABET, alphabetStrings);
        sb.append(",\n");

        appendStringArray(sb, KEY_STATES, states);
        sb.append(",\n");

        sb.append("  \"initialState\": \"").append(escapeJsonString(initialState)).append("\",\n");

        appendStringArray(sb, KEY_ACCEPTING_STATES, acceptingStates);
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

    /** Lightweight, zero-dependency recursive-descent parser compliant with RFC 8259. */
    private static final class JsonParser {

        private final String input;
        private int pos;
        private int depth;

        JsonParser(String input) {
            this.input = input;
            this.pos = 0;
            this.depth = 0;
        }

        Map<String, Object> parseRootObject() {
            skipWhitespace();
            Map<String, Object> root = parseObject();
            skipWhitespace();
            if (pos != input.length()) {
                throw new IllegalArgumentException(
                        "Unexpected trailing character at index " + pos + ": '" + peek() + "'");
            }
            return root;
        }

        private Map<String, Object> parseObject() {
            expect('{');
            enterContainer();
            skipWhitespace();

            Map<String, Object> map = new LinkedHashMap<>();
            if (match('}')) {
                exitContainer();
                return map;
            }

            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                Object value = parseValue();
                if (map.put(key, value) != null) {
                    throw new IllegalArgumentException("Duplicate JSON key: '" + key + "'");
                }
                skipWhitespace();
                if (match(',')) {
                    continue;
                }
                expect('}');
                break;
            }

            exitContainer();
            return map;
        }

        private List<Object> parseArray() {
            expect('[');
            enterContainer();
            skipWhitespace();

            List<Object> list = new ArrayList<>();
            if (match(']')) {
                exitContainer();
                return list;
            }

            while (true) {
                skipWhitespace();
                list.add(parseValue());
                skipWhitespace();
                if (match(',')) {
                    continue;
                }
                expect(']');
                break;
            }

            exitContainer();
            return list;
        }

        private Object parseValue() {
            if (!hasNext()) {
                throw new IllegalArgumentException("Unexpected end of JSON input at index " + pos);
            }
            char c = peek();
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 'n' -> parseNull();
                case 't', 'f' -> parseBoolean();
                default -> {
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        yield parseNumber();
                    }
                    throw new IllegalArgumentException(
                            "Unexpected JSON token at index " + pos + ": '" + c + "'");
                }
            };
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (hasNext()) {
                char c = next();
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    sb.append(parseEscapeSequence());
                } else if (c < 0x20) {
                    throw new IllegalArgumentException(
                            "Unescaped control character in JSON string at index " + (pos - 1));
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("Unterminated JSON string");
        }

        private char parseEscapeSequence() {
            if (!hasNext()) {
                throw new IllegalArgumentException(
                        "Incomplete escape sequence at end of JSON input");
            }
            char escaped = next();
            return switch (escaped) {
                case '"' -> '"';
                case '\\' -> '\\';
                case '/' -> '/';
                case 'b' -> '\b';
                case 'f' -> '\f';
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 't' -> '\t';
                case 'u' -> parseUnicodeEscape();
                default ->
                        throw new IllegalArgumentException(
                                "Invalid escape sequence '\\"
                                        + escaped
                                        + "' at index "
                                        + (pos - 1));
            };
        }

        private char parseUnicodeEscape() {
            if (pos + 4 > input.length()) {
                throw new IllegalArgumentException(
                        "Incomplete unicode escape sequence at index " + pos);
            }
            int codeUnit = 0;
            for (int i = 0; i < 4; i++) {
                char hex = next();
                int digit = Character.digit(hex, 16);
                if (digit < 0) {
                    throw new IllegalArgumentException(
                            "Invalid hex digit '" + hex + "' in unicode escape");
                }
                codeUnit = (codeUnit << 4) | digit;
            }
            return (char) codeUnit;
        }

        private Object parseNull() {
            expectLiteral("null");
            return JSON_NULL;
        }

        private Boolean parseBoolean() {
            if (peek() == 't') {
                expectLiteral("true");
                return Boolean.TRUE;
            }
            expectLiteral("false");
            return Boolean.FALSE;
        }

        private Number parseNumber() {
            int start = pos;
            match('-');
            consumeDigits();
            if (match('.')) {
                consumeDigits();
            }
            if (match('e') || match('E')) {
                if (!match('+')) {
                    match('-');
                }
                consumeDigits();
            }
            String token = input.substring(start, pos);
            try {
                return Double.valueOf(token);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid JSON number: '" + token + "'", ex);
            }
        }

        private void consumeDigits() {
            int start = pos;
            while (hasNext() && peek() >= '0' && peek() <= '9') {
                pos++;
            }
            if (pos == start) {
                throw new IllegalArgumentException("Expected digit at index " + pos);
            }
        }

        private void expectLiteral(String literal) {
            if (!input.startsWith(literal, pos)) {
                throw new IllegalArgumentException("Expected '" + literal + "' at index " + pos);
            }
            pos += literal.length();
        }

        private void enterContainer() {
            depth++;
            if (depth > MAX_NESTING_DEPTH) {
                throw new IllegalArgumentException(
                        "JSON nesting depth exceeds maximum allowed depth of "
                                + MAX_NESTING_DEPTH
                                + " (Security: DoS prevention).");
            }
        }

        private void exitContainer() {
            depth--;
        }

        private void skipWhitespace() {
            while (hasNext()) {
                char c = peek();
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    pos++;
                } else {
                    break;
                }
            }
        }

        private boolean hasNext() {
            return pos < input.length();
        }

        private char peek() {
            return input.charAt(pos);
        }

        private char next() {
            return input.charAt(pos++);
        }

        private boolean match(char expected) {
            if (hasNext() && peek() == expected) {
                pos++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            if (!hasNext() || peek() != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "' at index " + pos);
            }
            pos++;
        }
    }
}
