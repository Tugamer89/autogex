package org.eu.autogex.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
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

        // 1. Optimize epsilon closure computation using Tarjan's SCC globally
        Map<State, Set<State>> epsilonClosures = computeEpsilonClosures(enfa);

        // 2. Add states and recalculate final states based on closures
        for (State s : enfa.getStates()) {
            Set<State> closure = epsilonClosures.get(s);
            boolean isFinal = isFinal(closure, enfa.getFinalStates());
            builder.addState(s.getName(), isFinal);
        }

        // The initial state remains the same
        builder.setInitialState(enfa.getInitialState().getName());

        // 3. Compute new transitions for each state
        for (State q : enfa.getStates()) {
            Set<State> qClosure = epsilonClosures.get(q);

            for (State p : qClosure) {
                addReachableTransitions(enfa, builder, epsilonClosures, q.getName(), p);
            }
        }
        return builder.build();
    }

    private static void addReachableTransitions(
            ENFA enfa,
            NFA.Builder builder,
            Map<State, Set<State>> epsilonClosures,
            String sourceName,
            State p) {
        Map<Character, Set<State>> transitions = enfa.getTransitionTable().get(p);
        if (transitions != null) {
            for (Map.Entry<Character, Set<State>> entry : transitions.entrySet()) {
                Character a = entry.getKey();
                if (a != null) {
                    addTransitionsForSymbol(
                            builder, epsilonClosures, sourceName, a, entry.getValue());
                }
            }
        }
    }

    private static void addTransitionsForSymbol(
            NFA.Builder builder,
            Map<State, Set<State>> epsilonClosures,
            String sourceName,
            Character a,
            Set<State> targetStates) {
        for (State st : targetStates) {
            for (State target : epsilonClosures.get(st)) {
                builder.addTransition(sourceName, a, target.getName());
            }
        }
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
                        Set<State> targets =
                                symbolToTargets.computeIfAbsent(symbol, k -> new HashSet<>());
                        targets.addAll(entry.getValue());
                    }
                }
            }

            for (Map.Entry<Character, Set<State>> entry : symbolToTargets.entrySet()) {
                char symbol = entry.getKey();
                Set<State> nextSuperState = entry.getValue();

                String targetName =
                        dfaStateNames.computeIfAbsent(
                                nextSuperState,
                                k -> {
                                    if (dfaStateNames.size() >= MAX_DFA_STATES) {
                                        throw new IllegalStateException(
                                                "DFA state limit exceeded (Security: DoS prevention).");
                                    }
                                    String newTargetName = "D" + dfaStateNames.size();
                                    builder.addState(
                                            newTargetName,
                                            isFinal(nextSuperState, nfa.getFinalStates()));
                                    queue.add(nextSuperState);
                                    return newTargetName;
                                });
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

    /**
     * Computes the epsilon closures for all states in an ENFA globally. Uses Tarjan's Strongly
     * Connected Components algorithm to find a reverse topological sort, and propagates closures
     * bottom-up. This avoids redundant graph traversals.
     *
     * @param enfa The source ENFA.
     * @return A map of state to its epsilon closure set.
     */
    private static Map<State, Set<State>> computeEpsilonClosures(ENFA enfa) {
        Set<State> states = enfa.getStates();
        Map<State, Set<State>> epsGraph = buildEpsilonGraph(states, enfa.getTransitionTable());
        List<Set<State>> sccs = findSCCs(states, epsGraph);
        return propagateClosures(sccs, epsGraph);
    }

    private static Map<State, Set<State>> buildEpsilonGraph(
            Set<State> states, Map<State, Map<Character, Set<State>>> transitionTable) {
        Map<State, Set<State>> epsGraph = new HashMap<>();
        for (State s : states) {
            Map<Character, Set<State>> transitions = transitionTable.get(s);
            if (transitions != null) {
                Set<State> eps = transitions.get(null);
                if (eps != null && !eps.isEmpty()) {
                    epsGraph.put(s, eps);
                }
            }
        }
        return epsGraph;
    }

    private static List<Set<State>> findSCCs(Set<State> states, Map<State, Set<State>> epsGraph) {
        Map<State, Integer> indices = new HashMap<>();
        Map<State, Integer> lowlinks = new HashMap<>();
        Deque<State> stack = new ArrayDeque<>();
        Set<State> onStack = new HashSet<>();
        List<Set<State>> sccs = new ArrayList<>();
        int[] index = {0};

        for (State v : states) {
            if (!indices.containsKey(v)) {
                strongconnect(v, epsGraph, indices, lowlinks, stack, onStack, sccs, index);
            }
        }
        return sccs;
    }

    private static void strongconnect(
            State v,
            Map<State, Set<State>> epsGraph,
            Map<State, Integer> indices,
            Map<State, Integer> lowlinks,
            Deque<State> stack,
            Set<State> onStack,
            List<Set<State>> sccs,
            int[] index) {
        indices.put(v, index[0]);
        lowlinks.put(v, index[0]);
        index[0]++;
        stack.push(v);
        onStack.add(v);

        Set<State> edges = epsGraph.get(v);
        if (edges != null) {
            for (State w : edges) {
                if (!indices.containsKey(w)) {
                    strongconnect(w, epsGraph, indices, lowlinks, stack, onStack, sccs, index);
                    lowlinks.put(v, Math.min(lowlinks.get(v), lowlinks.get(w)));
                } else if (onStack.contains(w)) {
                    lowlinks.put(v, Math.min(lowlinks.get(v), indices.get(w)));
                }
            }
        }

        if (lowlinks.get(v).equals(indices.get(v))) {
            Set<State> scc = new HashSet<>();
            State w;
            do {
                w = stack.pop();
                onStack.remove(w);
                scc.add(w);
            } while (!w.equals(v));
            sccs.add(scc);
        }
    }

    private static Map<State, Set<State>> propagateClosures(
            List<Set<State>> sccs, Map<State, Set<State>> epsGraph) {
        Map<State, Set<State>> closures = new HashMap<>();
        for (Set<State> scc : sccs) {
            Set<State> sccClosure = new HashSet<>(scc);
            for (State v : scc) {
                Set<State> edges = epsGraph.get(v);
                if (edges != null) {
                    for (State w : edges) {
                        if (!scc.contains(w)) {
                            sccClosure.addAll(closures.get(w));
                        }
                    }
                }
            }
            for (State v : scc) {
                closures.put(v, sccClosure);
            }
        }
        return closures;
    }

    private static boolean isFinal(Set<State> superState, Set<State> finalStates) {
        return !Collections.disjoint(superState, finalStates);
    }
}
