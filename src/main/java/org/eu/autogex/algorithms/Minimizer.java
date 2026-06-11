package org.eu.autogex.algorithms;

import java.util.*;
import java.util.ArrayDeque;
import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;

/** Utility class for DFA minimization. Implements Moore's partition refinement algorithm. */
public class Minimizer {

    private Minimizer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Minimizes a DFA, returning a new equivalent DFA with the minimum number of states.
     *
     * @param dfa The source DFA to minimize.
     * @return The minimal DFA.
     */
    public static DFA minimize(DFA dfa) {
        // 1. Remove unreachable states
        Set<State> reachableStates = getReachableStates(dfa);
        Set<Character> alphabet = getAlphabet(dfa);
        char[] alphabetArray = new char[alphabet.size()];
        int idx = 0;
        for (char c : alphabet) {
            alphabetArray[idx++] = c;
        }

        // 2. Initial partition (Finals vs Non-Finals)
        Set<Set<State>> partitions = createInitialPartitions(dfa, reachableStates);

        Map<State, Integer> stateToPartitionId = new HashMap<>();
        updateStateToPartitionMap(partitions, stateToPartitionId);

        // 3. Iterative partition refinement (until Fixed Point is reached)
        boolean changed = true;
        while (changed) {
            changed = false;
            Set<Set<State>> newPartitions = new HashSet<>();

            for (Set<State> group : partitions) {
                // Split the group based on behavior (transition destinations)
                Map<BehaviorSignature, Set<State>> subGroups =
                        splitGroup(dfa, group, alphabetArray, stateToPartitionId);

                newPartitions.addAll(subGroups.values());

                // If a group was split into 2 or more subgroups, the partition has changed
                if (subGroups.size() > 1) {
                    changed = true;
                }
            }
            partitions = newPartitions;

            if (changed) {
                updateStateToPartitionMap(partitions, stateToPartitionId);
            }
        }

        // 4. Rebuild the Minimized DFA
        return buildMinimalDfa(dfa, partitions, stateToPartitionId);
    }

    private static void updateStateToPartitionMap(
            Set<Set<State>> partitions, Map<State, Integer> stateToPartitionId) {
        stateToPartitionId.clear();
        int partitionId = 0;
        for (Set<State> partition : partitions) {
            for (State state : partition) {
                stateToPartitionId.put(state, partitionId);
            }
            partitionId++;
        }
    }

    // --- Helper Methods ---

    private static Set<Set<State>> createInitialPartitions(DFA dfa, Set<State> reachableStates) {
        Set<State> finalGroup = new HashSet<>();
        Set<State> nonFinalGroup = new HashSet<>();

        for (State s : reachableStates) {
            if (dfa.getFinalStates().contains(s)) {
                finalGroup.add(s);
            } else {
                nonFinalGroup.add(s);
            }
        }

        Set<Set<State>> partitions = new HashSet<>();
        if (!finalGroup.isEmpty()) partitions.add(finalGroup);
        if (!nonFinalGroup.isEmpty()) partitions.add(nonFinalGroup);

        return partitions;
    }

    private static final class BehaviorSignature {
        private final int[] targets;
        private final int hashCode;

        public BehaviorSignature(int[] targets) {
            this.targets = targets;
            this.hashCode = Arrays.hashCode(targets);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BehaviorSignature that = (BehaviorSignature) o;
            return Arrays.equals(targets, that.targets);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    private static Map<BehaviorSignature, Set<State>> splitGroup(
            DFA dfa,
            Set<State> group,
            char[] alphabetArray,
            Map<State, Integer> stateToPartitionId) {

        // Maps the behavioral "signature" of a state to the subgroup of states sharing it
        Map<BehaviorSignature, Set<State>> subGroups = new HashMap<>();
        Map<State, Map<Character, State>> transitionTable = dfa.getTransitionTable();

        for (State s : group) {
            // The signature is: "For each character, which partition do I end up in?"
            int[] targets = new int[alphabetArray.length];
            Map<Character, State> transitions = transitionTable.get(s);

            if (transitions == null) {
                Arrays.fill(targets, -1);
            } else {
                for (int i = 0; i < alphabetArray.length; i++) {
                    State destination = transitions.get(alphabetArray[i]);
                    Integer targetPartitionId =
                            destination != null ? stateToPartitionId.get(destination) : null;
                    targets[i] = targetPartitionId != null ? targetPartitionId : -1;
                }
            }
            BehaviorSignature behaviorSignature = new BehaviorSignature(targets);

            subGroups.computeIfAbsent(behaviorSignature, k -> new HashSet<>()).add(s);
        }

        return subGroups;
    }

    private static DFA buildMinimalDfa(
            DFA originalDfa, Set<Set<State>> partitions, Map<State, Integer> stateToPartitionId) {
        DFA.Builder builder = new DFA.Builder();
        Map<Integer, String> partitionIdToName = new HashMap<>();

        // Register new states
        for (Set<State> partition : partitions) {
            State representative = partition.iterator().next();
            int partId = stateToPartitionId.get(representative);
            String name = "M" + partId;
            partitionIdToName.put(partId, name);

            // The partition is final if it contains at least one original final state.
            boolean isFinal = !Collections.disjoint(partition, originalDfa.getFinalStates());
            builder.addState(name, isFinal);

            // The partition is initial if it contains the original initial state
            if (partition.contains(originalDfa.getInitialState())) {
                builder.setInitialState(name);
            }
        }

        // Create transitions (taking a "representative" element for each partition is sufficient)
        Map<State, Map<Character, State>> transitionTable = originalDfa.getTransitionTable();
        for (Set<State> partition : partitions) {
            State representative = partition.iterator().next();
            int partId = stateToPartitionId.get(representative);
            String currentName = partitionIdToName.get(partId);

            Map<Character, State> transitions = transitionTable.get(representative);
            if (transitions != null) {
                for (Map.Entry<Character, State> entry : transitions.entrySet()) {
                    char symbol = entry.getKey();
                    State dest = entry.getValue();
                    Integer targetPartitionId = stateToPartitionId.get(dest);

                    // Link the transition if valid
                    if (targetPartitionId != null) {
                        builder.addTransition(
                                currentName, symbol, partitionIdToName.get(targetPartitionId));
                    }
                }
            }
        }

        return builder.build();
    }

    private static Set<State> getReachableStates(DFA dfa) {
        Set<State> reachable = new HashSet<>();
        // ArrayDeque is preferred over LinkedList for queues in hot paths.
        // It provides better cache locality and avoids O(n) node allocation overhead.
        Queue<State> queue = new ArrayDeque<>();

        reachable.add(dfa.getInitialState());
        queue.add(dfa.getInitialState());

        while (!queue.isEmpty()) {
            State current = queue.poll();
            Map<Character, State> transitions = dfa.getTransitionTable().get(current);

            if (transitions != null) {
                for (State nextState : transitions.values()) {
                    if (reachable.add(nextState)) {
                        queue.add(nextState);
                    }
                }
            }
        }
        return reachable;
    }

    private static Set<Character> getAlphabet(DFA dfa) {
        Set<Character> alphabet = new HashSet<>();
        for (Map<Character, State> transitions : dfa.getTransitionTable().values()) {
            alphabet.addAll(transitions.keySet());
        }
        return alphabet;
    }
}
