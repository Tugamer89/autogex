package org.eu.autogex.export;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;

/**
 * Utility class for exporting automata as GitHub-flavored Markdown state-transition tables. The
 * output is meant for documentation, READMEs and debugging.
 *
 * <p>Rows are states, sorted by name. The initial state is prefixed with {@code ->} and accepting
 * states with {@code *}. Columns are the symbols of the alphabet in natural order, followed by an
 * {@code ε} column for an {@link ENFA}. A DFA cell holds a single target state, or {@code -} when
 * the transition is undefined. NFA and ENFA cells hold a sorted set such as {@code {q1, q2}}, or
 * {@code ∅} when there is no transition.
 */
public class MarkdownTableExporter extends AbstractExporter {

    private static final String UNDEFINED_DFA_CELL = "-";
    private static final String EMPTY_SET_CELL = "∅";

    private MarkdownTableExporter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Exports a DFA to a Markdown table.
     *
     * @param dfa The Deterministic Finite Automaton.
     * @return The Markdown table, ending with a newline.
     */
    public static String toMarkdownTable(DFA dfa) {
        Map<State, Map<Character, Set<State>>> table = new LinkedHashMap<>();
        traverseTransitions(
                dfa,
                (source, label, target) ->
                        table.computeIfAbsent(source, s -> new LinkedHashMap<>())
                                .computeIfAbsent(label.charAt(0), c -> new TreeSet<>(BY_NAME))
                                .add(target));
        return render(
                dfa.getStates(), dfa.getInitialState(), dfa.getFinalStates(), table, false, true);
    }

    /**
     * Exports an NFA to a Markdown table.
     *
     * @param nfa The Non-Deterministic Finite Automaton.
     * @return The Markdown table, ending with a newline.
     */
    public static String toMarkdownTable(NFA nfa) {
        return render(
                nfa.getStates(),
                nfa.getInitialState(),
                nfa.getFinalStates(),
                nfa.getTransitionTable(),
                false,
                false);
    }

    /**
     * Exports an ENFA to a Markdown table. Epsilon transitions get their own {@code ε} column,
     * which is always present.
     *
     * @param enfa The Epsilon-NFA.
     * @return The Markdown table, ending with a newline.
     */
    public static String toMarkdownTable(ENFA enfa) {
        return render(
                enfa.getStates(),
                enfa.getInitialState(),
                enfa.getFinalStates(),
                enfa.getTransitionTable(),
                true,
                false);
    }

    // --- Private Helper Methods ---

    private static final Comparator<State> BY_NAME = Comparator.comparing(State::getName);

    private static String render(
            Set<State> states,
            State initialState,
            Set<State> finalStates,
            Map<State, Map<Character, Set<State>>> table,
            boolean withEpsilon,
            boolean deterministic) {
        Set<Character> alphabet = new TreeSet<>();
        for (Map<Character, Set<State>> row : table.values()) {
            for (Character symbol : row.keySet()) {
                if (symbol != null) {
                    alphabet.add(symbol);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("| State |");
        for (Character symbol : alphabet) {
            sb.append(' ').append(escapeCell(symbol.toString())).append(" |");
        }
        if (withEpsilon) {
            sb.append(' ').append(EPSILON_LABEL).append(" |");
        }
        sb.append('\n');

        sb.append("| --- |");
        for (int i = 0; i < alphabet.size() + (withEpsilon ? 1 : 0); i++) {
            sb.append(" --- |");
        }
        sb.append('\n');

        List<State> sortedStates = states.stream().sorted(BY_NAME).collect(Collectors.toList());
        for (State state : sortedStates) {
            Map<Character, Set<State>> row = table.getOrDefault(state, Collections.emptyMap());
            sb.append("| ").append(rowLabel(state, initialState, finalStates)).append(" |");
            for (Character symbol : alphabet) {
                sb.append(' ').append(cell(row.get(symbol), deterministic)).append(" |");
            }
            if (withEpsilon) {
                sb.append(' ').append(cell(row.get(null), false)).append(" |");
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static String rowLabel(State state, State initialState, Set<State> finalStates) {
        StringBuilder label = new StringBuilder();
        if (state.equals(initialState)) {
            label.append("-> ");
        }
        if (finalStates.contains(state)) {
            label.append('*');
        }
        return label.append(escapeCell(state.getName())).toString();
    }

    private static String cell(Set<State> targets, boolean deterministic) {
        if (targets == null || targets.isEmpty()) {
            return deterministic ? UNDEFINED_DFA_CELL : EMPTY_SET_CELL;
        }
        String names =
                targets.stream()
                        .map(State::getName)
                        .sorted()
                        .map(MarkdownTableExporter::escapeCell)
                        .collect(Collectors.joining(", "));
        return deterministic ? names : "{" + names + "}";
    }

    /** Escapes characters that would break a Markdown table cell. */
    private static String escapeCell(String text) {
        if (text == null) return "";
        return text.replace("\r", "").replace("\n", " ").replace("\\", "\\\\").replace("|", "\\|");
    }
}
