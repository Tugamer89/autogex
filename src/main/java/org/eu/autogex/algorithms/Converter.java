package org.eu.autogex.algorithms;

import java.util.*;
import java.util.ArrayDeque;
import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;

/** Utility class for Finite State Automata conversion. */
public class Converter {

    /** Maximum allowed states in a DFA to prevent state explosion (DoS). */
    private static final int MAX_DFA_STATES = 10000;

    private Converter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts an ε-NFA into an NFA. Applies the ε-elimination algorithm.
     *
     * @param enfa The source ε-NFA.
     * @return The equivalent NFA.
     */
    public static NFA enfaToNfa(ENFA enfa) {
        NFA.Builder builder = new NFA.Builder();

        Map<State, Set<State>> epsilonClosures = new HashMap<>();

        // 1. & 2. Add states and recalculate final states based on closures
        for (State s : enfa.getStates()) {
            Set<State> closure = enfa.epsilonClosure(Set.of(s));
            epsilonClosures.put(s, closure);
            boolean isFinal = isFinal(closure, enfa.getFinalStates());
            builder.addState(s.getName(), isFinal);
        }

        // The initial state remains the same
        builder.setInitialState(enfa.getInitialState().getName());

        // 3. Compute new transitions for each state
        for (State q : enfa.getStates()) {
            Set<State> qClosure = epsilonClosures.get(q);

            Map<Character, Set<State>> reachableTargets = new HashMap<>();

            for (State p : qClosure) {
                Map<Character, Set<State>> transitions = enfa.getTransitionTable().get(p);
                if (transitions != null) {
                    for (Map.Entry<Character, Set<State>> entry : transitions.entrySet()) {
                        Character a = entry.getKey();
                        if (a == null) continue;

                        Set<State> targetsForA = reachableTargets.get(a);
                        if (targetsForA == null) {
                            targetsForA = new HashSet<>();
                            reachableTargets.put(a, targetsForA);
                        }
                        for (State st : entry.getValue()) {
                            targetsForA.addAll(epsilonClosures.get(st));
                        }
                    }
                }
            }

            for (Map.Entry<Character, Set<State>> entry : reachableTargets.entrySet()) {
                char a = entry.getKey();
                for (State target : entry.getValue()) {
                    builder.addTransition(q.getName(), a, target.getName());
                }
            }
        }
        return builder.build();
    }

    /**
     * Converts an NFA into a DFA. Applies the Rabin-Scott Subset Construction algorithm.
     *
     * @param nfa The source NFA.
     * @return The equivalent DFA.
     */
    public static DFA nfaToDfa(NFA nfa) {
        DFA.Builder builder = new DFA.Builder();

        Map<Set<State>, String> dfaStateNames = new HashMap<>();
        // ArrayDeque is preferred over LinkedList for queues in hot paths.
        // It provides better cache locality and avoids O(n) node allocation overhead.
        Queue<Set<State>> queue = new ArrayDeque<>();

        // The DFA initial state is the set containing only the NFA's initial state
        Set<State> initialSuperState = Set.of(nfa.getInitialState());
        String initialName = "D0";

        builder.addState(initialName, isFinal(initialSuperState, nfa.getFinalStates()));
        builder.setInitialState(initialName);

        dfaStateNames.put(initialSuperState, initialName);
        queue.add(initialSuperState);

        // Explore all possible subsets
        while (!queue.isEmpty()) {
            Set<State> currentSuperState = queue.poll();
            String currentName = dfaStateNames.get(currentSuperState);

            Map<Character, Set<State>> symbolToTargets = new HashMap<>();
            for (State s : currentSuperState) {
                Map<Character, Set<State>> transitions = nfa.getTransitionTable().get(s);
                if (transitions != null) {
                    for (Map.Entry<Character, Set<State>> entry : transitions.entrySet()) {
                        Character symbol = entry.getKey();
                        Set<State> targets = symbolToTargets.get(symbol);
                        if (targets == null) {
                            targets = new HashSet<>();
                            symbolToTargets.put(symbol, targets);
                        }
                        targets.addAll(entry.getValue());
                    }
                }
            }

            for (Map.Entry<Character, Set<State>> entry : symbolToTargets.entrySet()) {
                char symbol = entry.getKey();
                Set<State> nextSuperState = entry.getValue();

                String targetName = dfaStateNames.get(nextSuperState);
                if (targetName == null) {
                    if (dfaStateNames.size() >= MAX_DFA_STATES) {
                        throw new IllegalStateException(
                                "DFA state limit exceeded (Security: DoS prevention).");
                    }
                    targetName = "D" + dfaStateNames.size();
                    builder.addState(targetName, isFinal(nextSuperState, nfa.getFinalStates()));
                    dfaStateNames.put(nextSuperState, targetName);
                    queue.add(nextSuperState);
                }
                builder.addTransition(currentName, symbol, targetName);
            }
        }

        return builder.build();
    }

    /**
     * Convenience method that applies the full transformation chain: ENFA -> NFA -> DFA.
     *
     * @param enfa The source ε-NFA.
     * @return The equivalent DFA.
     */
    public static DFA enfaToDfa(ENFA enfa) {
        NFA intermediateNfa = enfaToNfa(enfa);
        return nfaToDfa(intermediateNfa);
    }

    // --- Helper Methods ---

    private static boolean isFinal(Set<State> superState, Set<State> finalStates) {
        return !Collections.disjoint(superState, finalStates);
    }
}
