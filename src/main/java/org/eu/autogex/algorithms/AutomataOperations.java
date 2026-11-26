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
import org.eu.autogex.core.Automaton;
import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;

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

    private static DFA toDfa(Automaton automaton) {
        return switch (automaton) {
            case DFA dfa -> dfa;
            case NFA nfa -> Converter.nfaToDfa(nfa);
            case ENFA enfa -> Converter.enfaToDfa(enfa);
            default ->
                    throw new IllegalArgumentException(
                            "Unsupported automaton type: " + automaton.getClass().getName());
        };
    }

    /**
     * Checks whether two finite automata recognize the exact same regular language over the union
     * of their alphabets.
     *
     * <p>Two automata {@code a1} and {@code a2} are equivalent if and only if their symmetric
     * difference is empty: {@code (L(a1) \ L(a2)) U (L(a2) \ L(a1)) = empty}. Inputs of type {@link
     * DFA}, {@link NFA}, or {@link ENFA} are normalized to {@link DFA} instances, evaluated across
     * the combined alphabet, and minimized so emptiness is determined by {@code
     * minimized.getFinalStates().isEmpty()}.
     *
     * <p><b>Complexity:</b> Worst-case time and space complexity are exponential in the number of
     * states when converting {@link NFA} or {@link ENFA} inputs to {@link DFA} via subset
     * construction; for already-deterministic inputs, operations scale polynomially with state
     * count and alphabet size.
     *
     * <p><b>Example:</b>
     *
     * <pre>{@code
     * boolean sameLanguage = AutomataOperations.areEquivalent(dfa1, nfa2);
     * }</pre>
     *
     * @param a1 the first finite automaton, must not be {@code null}
     * @param a2 the second finite automaton, must not be {@code null}
     * @return {@code true} if {@code a1} and {@code a2} accept the exact same language, {@code
     *     false} otherwise
     * @throws NullPointerException if {@code a1} or {@code a2} is {@code null}
     */
    public static boolean areEquivalent(Automaton a1, Automaton a2) {
        Objects.requireNonNull(a1, "a1 must not be null");
        Objects.requireNonNull(a2, "a2 must not be null");

        DFA dfa1 = toDfa(a1);
        DFA dfa2 = toDfa(a2);

        Set<Character> combinedAlphabet = new LinkedHashSet<>(extractAlphabet(dfa1));
        combinedAlphabet.addAll(extractAlphabet(dfa2));

        DFA completed1 = completeWithDeadState(dfa1, combinedAlphabet);
        DFA completed2 = completeWithDeadState(dfa2, combinedAlphabet);

        DFA diff1 = difference(completed1, completed2);
        DFA diff2 = difference(completed2, completed1);
        DFA symmetricDifference = union(diff1, diff2);

        DFA minimizedDiff = Minimizer.minimize(symmetricDifference);
        return minimizedDiff.getFinalStates().isEmpty();
    }

    private static DFA completeWithDeadState(DFA dfa, Set<Character> alphabet) {
        Map<State, Map<Character, State>> transitions = dfa.getTransitionTable();
        boolean needsDeadState = hasMissingTransitions(dfa.getStates(), transitions, alphabet);

        DFA.Builder builder = new DFA.Builder();
        for (State state : dfa.getStates()) {
            builder.addState(state.getName(), state.isFinal());
        }
        builder.setInitialState(dfa.getInitialState().getName());

        String deadName = null;
        if (needsDeadState) {
            deadName = generateUniqueSinkName(dfa.getStates());
            builder.addState(deadName, false);
        }

        for (State state : dfa.getStates()) {
            Map<Character, State> stateTransitions =
                    transitions.getOrDefault(state, Collections.emptyMap());
            for (char symbol : alphabet) {
                State target = stateTransitions.get(symbol);
                if (target != null) {
                    builder.addTransition(state.getName(), symbol, target.getName());
                } else if (deadName != null) {
                    builder.addTransition(state.getName(), symbol, deadName);
                }
            }
        }

        if (deadName != null) {
            for (char symbol : alphabet) {
                builder.addTransition(deadName, symbol, deadName);
            }
        }

        return builder.build();
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
