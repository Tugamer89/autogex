package org.eu.autogex.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;

/**
 * Utility class for generating valid strings accepted by a {@link DFA}, supporting both randomized
 * fuzzing walks and bounded breadth-first lexicographical enumeration.
 */
public final class LanguageGenerator {

    private static final String DFA_NULL_MSG = "DFA cannot be null";
    private static final String RANDOM_NULL_MSG = "RandomGenerator cannot be null";
    private static final String MAX_LENGTH_NEGATIVE_MSG = "maxLength must be non-negative";

    private record Edge(char symbol, State target) {}

    private record BfsNode(State state, String prefix) {}

    private LanguageGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Generates a random valid string accepted by {@code dfa} whose length does not exceed {@code
     * maxLength}.
     *
     * <p>The walk is guided by shortest-path distances to accepting states so that cyclic DFAs and
     * dead-end sink states cannot cause infinite loops or failed generations.
     *
     * @param dfa the deterministic finite automaton
     * @param maxLength the maximum allowed length of the generated string (inclusive)
     * @return a randomly generated string accepted by {@code dfa}
     * @throws NullPointerException if {@code dfa} is {@code null}
     * @throws IllegalArgumentException if {@code maxLength < 0} or if {@code dfa} accepts no
     *     strings of length {@code <= maxLength}
     */
    public static String generateRandom(DFA dfa, int maxLength) {
        return generateRandom(dfa, maxLength, ThreadLocalRandom.current());
    }

    /**
     * Generates a random valid string accepted by {@code dfa} using the supplied {@link
     * RandomGenerator}.
     *
     * @param dfa the deterministic finite automaton
     * @param maxLength the maximum allowed length of the generated string (inclusive)
     * @param random the random source used to select transitions and stopping points
     * @return a randomly generated string accepted by {@code dfa}
     * @throws NullPointerException if {@code dfa} or {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code maxLength < 0} or if {@code dfa} accepts no
     *     strings of length {@code <= maxLength}
     */
    public static String generateRandom(DFA dfa, int maxLength, RandomGenerator random) {
        Objects.requireNonNull(dfa, DFA_NULL_MSG);
        Objects.requireNonNull(random, RANDOM_NULL_MSG);
        if (maxLength < 0) {
            throw new IllegalArgumentException(MAX_LENGTH_NEGATIVE_MSG);
        }

        Map<State, Integer> minDistances = computeMinDistancesToFinal(dfa);
        State current = dfa.getInitialState();
        Integer initialDist = minDistances.get(current);

        if (initialDist == null || initialDist > maxLength) {
            throw new IllegalArgumentException(
                    "DFA accepts no strings with length <= " + maxLength);
        }

        Map<State, Map<Character, State>> transitionTable = dfa.getTransitionTable();
        StringBuilder result = new StringBuilder();

        while (result.length() < maxLength) {
            int remainingBudget = maxLength - result.length();
            List<Edge> viableTransitions =
                    getViableTransitions(
                            transitionTable.getOrDefault(current, Collections.emptyMap()),
                            minDistances,
                            remainingBudget);

            if (current.isFinal()) {
                if (viableTransitions.isEmpty()) {
                    return result.toString();
                }
                // Stop at the current accepting state with probability 1 / (k + 1)
                if (random.nextInt(viableTransitions.size() + 1) == 0) {
                    return result.toString();
                }
            }

            Edge chosen = viableTransitions.get(random.nextInt(viableTransitions.size()));
            result.append(chosen.symbol());
            current = chosen.target();
        }

        return result.toString();
    }

    /**
     * Enumerates all strings accepted by {@code dfa} up to {@code maxLength} (inclusive) in
     * canonical lexicographical order (ordered by increasing length, then alphabetically).
     *
     * <p>Uses a bounded Breadth-First Search (BFS) pruned to co-accessible states so cyclic DFAs
     * are safely traversed without entering infinite loops or exploring dead-end sink cycles.
     *
     * @param dfa the deterministic finite automaton
     * @param maxLength the maximum length of generated strings (inclusive)
     * @return an unmodifiable list of all accepted strings of length {@code <= maxLength}
     * @throws NullPointerException if {@code dfa} is {@code null}
     * @throws IllegalArgumentException if {@code maxLength < 0}
     */
    public static List<String> generateAll(DFA dfa, int maxLength) {
        Objects.requireNonNull(dfa, DFA_NULL_MSG);
        if (maxLength < 0) {
            throw new IllegalArgumentException(MAX_LENGTH_NEGATIVE_MSG);
        }

        Set<State> coAccessible = computeMinDistancesToFinal(dfa).keySet();
        State initial = dfa.getInitialState();
        if (!coAccessible.contains(initial)) {
            return List.of();
        }

        Map<State, List<Edge>> sortedTransitions =
                buildSortedCoAccessibleTransitions(dfa.getTransitionTable(), coAccessible);

        List<String> accepted = new ArrayList<>();
        Queue<BfsNode> queue = new ArrayDeque<>();
        queue.add(new BfsNode(initial, ""));

        while (!queue.isEmpty()) {
            BfsNode current = queue.poll();
            State state = current.state();
            String prefix = current.prefix();

            if (state.isFinal()) {
                accepted.add(prefix);
            }

            if (prefix.length() < maxLength) {
                for (Edge edge : sortedTransitions.getOrDefault(state, List.of())) {
                    queue.add(new BfsNode(edge.target(), prefix + edge.symbol()));
                }
            }
        }

        return Collections.unmodifiableList(accepted);
    }

    private static List<Edge> getViableTransitions(
            Map<Character, State> outgoing, Map<State, Integer> minDistances, int remainingBudget) {
        List<Edge> viable = new ArrayList<>();
        for (Map.Entry<Character, State> entry : outgoing.entrySet()) {
            Integer targetDist = minDistances.get(entry.getValue());
            if (targetDist != null && targetDist <= remainingBudget - 1) {
                viable.add(new Edge(entry.getKey(), entry.getValue()));
            }
        }
        viable.sort(Comparator.comparingInt(Edge::symbol));
        return viable;
    }

    private static Map<State, List<Edge>> buildSortedCoAccessibleTransitions(
            Map<State, Map<Character, State>> transitionTable, Set<State> coAccessible) {
        Map<State, List<Edge>> sorted = new HashMap<>();
        for (Map.Entry<State, Map<Character, State>> stateEntry : transitionTable.entrySet()) {
            State source = stateEntry.getKey();
            if (!coAccessible.contains(source)) {
                continue;
            }
            List<Edge> edges =
                    stateEntry.getValue().entrySet().stream()
                            .filter(entry -> coAccessible.contains(entry.getValue()))
                            .map(entry -> new Edge(entry.getKey(), entry.getValue()))
                            .sorted(Comparator.comparingInt(Edge::symbol))
                            .toList();
            sorted.put(source, edges);
        }
        return sorted;
    }

    /**
     * Computes the shortest path length from every co-accessible state to any accepting state via
     * reverse Breadth-First Search.
     */
    private static Map<State, Integer> computeMinDistancesToFinal(DFA dfa) {
        Set<State> finalStates = dfa.getFinalStates();
        if (finalStates.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<State, Set<State>> reverseEdges = new HashMap<>();
        for (Map.Entry<State, Map<Character, State>> entry : dfa.getTransitionTable().entrySet()) {
            State from = entry.getKey();
            for (State to : entry.getValue().values()) {
                reverseEdges.computeIfAbsent(to, k -> new HashSet<>()).add(from);
            }
        }

        Map<State, Integer> distances = new HashMap<>();
        Queue<State> queue = new ArrayDeque<>();

        for (State finalState : finalStates) {
            distances.put(finalState, 0);
            queue.add(finalState);
        }

        while (!queue.isEmpty()) {
            State current = queue.poll();
            int nextDist = distances.get(current) + 1;
            for (State predecessor : reverseEdges.getOrDefault(current, Collections.emptySet())) {
                distances.computeIfAbsent(
                        predecessor,
                        k -> {
                            queue.add(k);
                            return nextDist;
                        });
            }
        }

        return distances;
    }
}
