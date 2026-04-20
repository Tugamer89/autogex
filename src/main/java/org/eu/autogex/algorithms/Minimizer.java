package org.eu.autogex.algorithms;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;

/**
 * Utility class for DFA minimization.
 * Implements Moore's partition refinement algorithm.
 */
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

        // 2. Initial partition (Finals vs Non-Finals)
        Set<Set<State>> partitions = createInitialPartitions(dfa, reachableStates);

        // 3. Iterative partition refinement (until Fixed Point is reached)
        boolean changed = true;
        while (changed) {
            changed = false;
            Set<Set<State>> newPartitions = new HashSet<>();

            for (Set<State> group : partitions) {
                // Split the group based on behavior (transition destinations)
                Map<Map<Character, Set<State>>, Set<State>> subGroups = splitGroup(dfa, group, alphabet, partitions);
                
                newPartitions.addAll(subGroups.values());
                
                // If a group was split into 2 or more subgroups, the partition has changed
                if (subGroups.size() > 1) {
                    changed = true;
                }
            }
            partitions = newPartitions;
        }

        // 4. Rebuild the Minimized DFA
        return buildMinimalDfa(dfa, partitions, alphabet);
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

    private static Map<Map<Character, Set<State>>, Set<State>> splitGroup(
            DFA dfa, Set<State> group, Set<Character> alphabet, Set<Set<State>> currentPartitions) {
        
        // Maps the behavioral "signature" of a state to the subgroup of states sharing it
        Map<Map<Character, Set<State>>, Set<State>> subGroups = new HashMap<>();

        for (State s : group) {
            // The signature is: "For each character, which partition do I end up in?"
            Map<Character, Set<State>> behaviorSignature = new HashMap<>();
            
            for (char symbol : alphabet) {
                State destination = getDestination(dfa, s, symbol);
                Set<State> targetPartition = findPartitionContaining(currentPartitions, destination);
                behaviorSignature.put(symbol, targetPartition);
            }

            subGroups.computeIfAbsent(behaviorSignature, k -> new HashSet<>()).add(s);
        }
        
        return subGroups;
    }

    private static DFA buildMinimalDfa(DFA originalDfa, Set<Set<State>> partitions, Set<Character> alphabet) {
        DFA.Builder builder = new DFA.Builder();
        Map<Set<State>, String> partitionToName = new HashMap<>();
        AtomicInteger counter = new AtomicInteger(0);

        // Register new states
        for (Set<State> partition : partitions) {
            String name = "M" + counter.getAndIncrement();
            partitionToName.put(partition, name);
            
            // The partition is final if it contains at least one original final state
            boolean isFinal = partition.stream().anyMatch(s -> originalDfa.getFinalStates().contains(s));
            builder.addState(name, isFinal);

            // The partition is initial if it contains the original initial state
            if (partition.contains(originalDfa.getInitialState())) {
                builder.setInitialState(name);
            }
        }

        // Create transitions (taking a "representative" element for each partition is sufficient)
        for (Set<State> partition : partitions) {
            State representative = partition.iterator().next();
            String currentName = partitionToName.get(partition);

            for (char symbol : alphabet) {
                State dest = getDestination(originalDfa, representative, symbol);
                Set<State> targetPartition = findPartitionContaining(partitions, dest);
                
                // Link the transition if valid
                if (!targetPartition.isEmpty()) {
                    builder.addTransition(currentName, symbol, partitionToName.get(targetPartition));
                }
            }
        }

        return builder.build();
    }

    private static Set<State> getReachableStates(DFA dfa) {
        Set<State> reachable = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        
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

    private static State getDestination(DFA dfa, State source, char symbol) {
        Map<Character, State> transitions = dfa.getTransitionTable().get(source);
        return transitions != null ? transitions.get(symbol) : null;
    }

    private static Set<State> findPartitionContaining(Set<Set<State>> partitions, State target) {
        if (target == null) return Collections.emptySet();
        for (Set<State> partition : partitions) {
            if (partition.contains(target)) {
                return partition;
            }
        }
        return Collections.emptySet();
    }

    private static Set<Character> getAlphabet(DFA dfa) {
        Set<Character> alphabet = new HashSet<>();
        for (Map<Character, State> transitions : dfa.getTransitionTable().values()) {
            alphabet.addAll(transitions.keySet());
        }
        return alphabet;
    }
}