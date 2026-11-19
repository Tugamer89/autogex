package org.eu.autogex.algorithms;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;

/**
 * Utility class providing mathematical set operations on Deterministic Finite Automata (DFAs),
 * demonstrating the closure properties of regular languages under complement, intersection, union,
 * and set difference.
 */
public final class AutomataOperations {

    private static final String DFA_NULL_MSG = "DFA cannot be null";
    private static final String FIRST_DFA_NULL_MSG = "First DFA cannot be null";
    private static final String SECOND_DFA_NULL_MSG = "Second DFA cannot be null";
    private static final String ALPHABET_NULL_MSG = "Alphabet cannot be null";

    private record StatePair(State first, State second) {}

    private AutomataOperations() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Computes the complement ({@code L̅}) of the language recognized by the given {@link DFA} over
     * its derived transition alphabet.
     *
     * <p>If the input DFA has a partial transition function over its alphabet, an explicit sink
     * state is introduced prior to inverting accepting and non-accepting states so that all
     * rejected strings over the alphabet are accepted by the complement.
     *
     * @param dfa the input deterministic finite automaton
     * @return a new {@link DFA} accepting the complement language
     * @throws NullPointerException if {@code dfa} is {@code null}
     */
    public static DFA complement(DFA dfa) {
        Objects.requireNonNull(dfa, DFA_NULL_MSG);
        return complement(dfa, extractAlphabet(dfa));
    }

    /**
     * Computes the complement ({@code L̅}) of the language recognized by the given {@link DFA} with
     * respect to an explicit alphabet {@code alphabet}.
     *
     * @param dfa the input deterministic finite automaton
     * @param alphabet the reference alphabet over which to complete transitions
     * @return a new {@link DFA} accepting {@code alphabet* \ L(dfa)}
     * @throws NullPointerException if {@code dfa} or {@code alphabet} is {@code null}
     */
    public static DFA complement(DFA dfa, Set<Character> alphabet) {
        Objects.requireNonNull(dfa, DFA_NULL_MSG);
        Objects.requireNonNull(alphabet, ALPHABET_NULL_MSG);

        Set<Character> effectiveAlphabet = new LinkedHashSet<>(alphabet);
        effectiveAlphabet.addAll(extractAlphabet(dfa));

        Map<State, Map<Character, State>> transitions = dfa.getTransitionTable();
        boolean needsSink = hasMissingTransitions(dfa.getStates(), transitions, effectiveAlphabet);

        DFA.Builder builder = new DFA.Builder();

        for (State state : dfa.getStates()) {
            builder.addState(state.getName(), !state.isFinal());
        }
        builder.setInitialState(dfa.getInitialState().getName());

        String sinkName = null;
        if (needsSink) {
            sinkName = generateUniqueSinkName(dfa.getStates());
            builder.addState(sinkName, true);
        }

        for (State state : dfa.getStates()) {
            Map<Character, State> stateTransitions =
                    transitions.getOrDefault(state, Collections.emptyMap());
            for (char symbol : effectiveAlphabet) {
                State target = stateTransitions.get(symbol);
                if (target != null) {
                    builder.addTransition(state.getName(), symbol, target.getName());
                } else if (sinkName != null) {
                    builder.addTransition(state.getName(), symbol, sinkName);
                }
            }
        }

        if (sinkName != null) {
            for (char symbol : effectiveAlphabet) {
                builder.addTransition(sinkName, symbol, sinkName);
            }
        }

        return builder.build();
    }

    /**
     * Computes the intersection ({@code L1 ∩ L2}) of two deterministic finite automata using the
     * Cartesian product construction.
     *
     * @param first the first DFA
     * @param second the second DFA
     * @return a new {@link DFA} accepting strings accepted by both {@code first} and {@code second}
     * @throws NullPointerException if either DFA is {@code null}
     */
    public static DFA intersection(DFA first, DFA second) {
        Objects.requireNonNull(first, FIRST_DFA_NULL_MSG);
        Objects.requireNonNull(second, SECOND_DFA_NULL_MSG);
        return product(first, second, (f1, f2) -> f1 && f2);
    }

    /**
     * Computes the union ({@code L1 ∪ L2}) of two deterministic finite automata using the Cartesian
     * product construction.
     *
     * @param first the first DFA
     * @param second the second DFA
     * @return a new {@link DFA} accepting strings accepted by {@code first}, {@code second}, or
     *     both
     * @throws NullPointerException if either DFA is {@code null}
     */
    public static DFA union(DFA first, DFA second) {
        Objects.requireNonNull(first, FIRST_DFA_NULL_MSG);
        Objects.requireNonNull(second, SECOND_DFA_NULL_MSG);
        return product(first, second, (f1, f2) -> f1 || f2);
    }

    /**
     * Computes the set difference ({@code L1 \ L2}) of two deterministic finite automata using the
     * Cartesian product construction.
     *
     * @param first the minuend DFA ({@code L1})
     * @param second the subtrahend DFA ({@code L2})
     * @return a new {@link DFA} accepting strings accepted by {@code first} but rejected by {@code
     *     second}
     * @throws NullPointerException if either DFA is {@code null}
     */
    public static DFA difference(DFA first, DFA second) {
        Objects.requireNonNull(first, FIRST_DFA_NULL_MSG);
        Objects.requireNonNull(second, SECOND_DFA_NULL_MSG);
        return product(first, second, (f1, f2) -> f1 && !f2);
    }

    private static DFA product(
            DFA first, DFA second, BiPredicate<Boolean, Boolean> acceptanceCondition) {
        Set<Character> combinedAlphabet = new LinkedHashSet<>(extractAlphabet(first));
        combinedAlphabet.addAll(extractAlphabet(second));

        Map<State, Map<Character, State>> transitions1 = first.getTransitionTable();
        Map<State, Map<Character, State>> transitions2 = second.getTransitionTable();

        StatePair initialPair = new StatePair(first.getInitialState(), second.getInitialState());

        Map<StatePair, String> stateNames = new HashMap<>();
        Set<String> usedNames = new HashSet<>();
        Queue<StatePair> queue = new ArrayDeque<>();

        DFA.Builder builder = new DFA.Builder();

        String initialName = registerPair(initialPair, stateNames, usedNames);
        builder.addState(initialName, isPairAccepting(initialPair, acceptanceCondition));
        builder.setInitialState(initialName);
        queue.add(initialPair);

        while (!queue.isEmpty()) {
            StatePair current = queue.poll();
            String currentName = stateNames.get(current);

            Map<Character, State> map1 =
                    current.first() == null
                            ? Collections.emptyMap()
                            : transitions1.getOrDefault(current.first(), Collections.emptyMap());
            Map<Character, State> map2 =
                    current.second() == null
                            ? Collections.emptyMap()
                            : transitions2.getOrDefault(current.second(), Collections.emptyMap());

            for (char symbol : combinedAlphabet) {
                State next1 = map1.get(symbol);
                State next2 = map2.get(symbol);

                if (next1 == null && next2 == null) {
                    continue;
                }

                StatePair nextPair = new StatePair(next1, next2);
                String nextName = stateNames.get(nextPair);
                if (nextName == null) {
                    nextName = registerPair(nextPair, stateNames, usedNames);
                    builder.addState(nextName, isPairAccepting(nextPair, acceptanceCondition));
                    queue.add(nextPair);
                }

                builder.addTransition(currentName, symbol, nextName);
            }
        }

        return builder.build();
    }

    private static boolean isPairAccepting(
            StatePair pair, BiPredicate<Boolean, Boolean> acceptanceCondition) {
        boolean firstFinal = pair.first() != null && pair.first().isFinal();
        boolean secondFinal = pair.second() != null && pair.second().isFinal();
        return acceptanceCondition.test(firstFinal, secondFinal);
    }

    private static String registerPair(
            StatePair pair, Map<StatePair, String> stateNames, Set<String> usedNames) {
        String s1 = pair.first() != null ? pair.first().getName() : "⊥";
        String s2 = pair.second() != null ? pair.second().getName() : "⊥";
        String baseName = "(" + s1 + ", " + s2 + ")";

        String uniqueName = baseName;
        int counter = 1;
        while (!usedNames.add(uniqueName)) {
            uniqueName = baseName + "_" + counter++;
        }

        stateNames.put(pair, uniqueName);
        return uniqueName;
    }

    private static Set<Character> extractAlphabet(DFA dfa) {
        return dfa.getTransitionTable().values().stream()
                .flatMap(transitions -> transitions.keySet().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean hasMissingTransitions(
            Set<State> states,
            Map<State, Map<Character, State>> transitions,
            Set<Character> alphabet) {
        if (alphabet.isEmpty()) {
            return false;
        }
        for (State state : states) {
            Map<Character, State> outgoing =
                    transitions.getOrDefault(state, Collections.emptyMap());
            for (char symbol : alphabet) {
                if (!outgoing.containsKey(symbol)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String generateUniqueSinkName(Set<State> existingStates) {
        Set<String> existingNames =
                existingStates.stream().map(State::getName).collect(Collectors.toSet());
        String candidate = "q_sink";
        int index = 1;
        while (existingNames.contains(candidate)) {
            candidate = "q_sink_" + index++;
        }
        return candidate;
    }
}
