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
        Set<Character> alphabet = getAlphabet(enfa.getTransitionTable());

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

        // 3. Compute new transitions for each state across the alphabet
        for (State q : enfa.getStates()) {
            Set<State> qClosure = epsilonClosures.get(q);

            for (char a : alphabet) {
                Set<State> targets = computeEnfaTargets(enfa, qClosure, a, epsilonClosures);
                for (State target : targets) {
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
        Set<Character> alphabet = getAlphabet(nfa.getTransitionTable());

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

            for (char symbol : alphabet) {
                processSymbolTransitions(
                        nfa, builder, dfaStateNames, queue, currentSuperState, currentName, symbol);
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

    /**
     * Processes transitions for a specific symbol from a given super-state in the subset
     * construction algorithm.
     */
    private static void processSymbolTransitions(
            NFA nfa,
            DFA.Builder builder,
            Map<Set<State>, String> dfaStateNames,
            Queue<Set<State>> queue,
            Set<State> currentSuperState,
            String currentName,
            char symbol) {
        Set<State> nextSuperState = computeNextSuperState(nfa, currentSuperState, symbol);

        if (!nextSuperState.isEmpty()) {
            String targetName =
                    dfaStateNames.computeIfAbsent(
                            nextSuperState,
                            k -> {
                                if (dfaStateNames.size() >= MAX_DFA_STATES) {
                                    throw new IllegalStateException(
                                            "DFA state limit exceeded (Security: DoS prevention).");
                                }
                                String nextName = "D" + dfaStateNames.size();
                                builder.addState(nextName, isFinal(k, nfa.getFinalStates()));
                                queue.add(k);
                                return nextName;
                            });
            builder.addTransition(currentName, symbol, targetName);
        }
    }

    /** Computes the reachable subset of states by reading a symbol. */
    private static Set<State> computeNextSuperState(
            NFA nfa, Set<State> currentSuperState, char symbol) {
        Set<State> nextSuperState = new HashSet<>();
        for (State s : currentSuperState) {
            Map<Character, Set<State>> transitions = nfa.getTransitionTable().get(s);
            if (transitions != null) {
                Set<State> targets = transitions.get(symbol);
                if (targets != null) {
                    nextSuperState.addAll(targets);
                }
            }
        }
        return nextSuperState;
    }

    /**
     * Computes target states for an ENFA starting from a closure, reading a symbol, and applying
     * the ε-closure to the result (utilizing pre-computed closures for performance).
     */
    private static Set<State> computeEnfaTargets(
            ENFA enfa, Set<State> qClosure, char symbol, Map<State, Set<State>> epsilonClosures) {
        Set<State> targets = new HashSet<>();
        for (State p : qClosure) {
            Map<Character, Set<State>> transitions = enfa.getTransitionTable().get(p);
            if (transitions != null) {
                Set<State> symbolTargets = transitions.get(symbol);
                if (symbolTargets != null) {
                    for (State st : symbolTargets) {
                        targets.addAll(epsilonClosures.get(st));
                    }
                }
            }
        }
        return targets;
    }

    private static Set<Character> getAlphabet(
            Map<State, Map<Character, Set<State>>> transitionTable) {
        Set<Character> alphabet = new HashSet<>();
        for (Map<Character, Set<State>> transitions : transitionTable.values()) {
            for (Character c : transitions.keySet()) {
                if (c == null) {
                    continue;
                }
                alphabet.add(c);
            }
        }
        return alphabet;
    }

    private static boolean isFinal(Set<State> superState, Set<State> finalStates) {
        return !Collections.disjoint(superState, finalStates);
    }
}
