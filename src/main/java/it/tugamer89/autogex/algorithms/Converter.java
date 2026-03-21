package it.tugamer89.autogex.algorithms;

import it.tugamer89.autogex.core.State;
import it.tugamer89.autogex.models.DFA;
import it.tugamer89.autogex.models.ENFA;
import it.tugamer89.autogex.models.NFA;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility per la conversione degli Automi a Stati Finiti.
 */
public class Converter {

    private Converter() {
        throw new UnsupportedOperationException("Classe di utility");
    }

    /**
     * Converte un ε-NFA in un NFA.
     * Algoritmo di ε-eliminazione.
     */
    public static NFA enfaToNfa(ENFA enfa) {
        NFA.Builder builder = new NFA.Builder();
        Set<Character> alphabet = getAlphabet(enfa.getTransitionTable());

        // 1. & 2. Aggiungiamo gli stati e ricalcoliamo gli stati finali
        for (State s : enfa.getStates()) {
            Set<State> closure = enfa.epsilonClosure(Set.of(s));
            boolean isFinal = isFinal(closure, enfa.getFinalStates());
            builder.addState(s.getName(), isFinal);
        }

        // Lo stato iniziale rimane lo stesso
        builder.setInitialState(enfa.getInitialState().getName());

        // 3. Calcoliamo le nuove transizioni per ogni stato dell'alfabeto
        for (State q : enfa.getStates()) {
            Set<State> qClosure = enfa.epsilonClosure(Set.of(q));
            
            for (char a : alphabet) {
                Set<State> targets = computeEnfaTargets(enfa, qClosure, a);
                for (State target : targets) {
                    builder.addTransition(q.getName(), a, target.getName());
                }
            }
        }
        return builder.build();
    }

    /**
     * Converte un NFA in un DFA.
     * Algoritmo Rabin-Scott.
     */
    public static DFA nfaToDfa(NFA nfa) {
        DFA.Builder builder = new DFA.Builder();
        Set<Character> alphabet = getAlphabet(nfa.getTransitionTable());

        Map<Set<State>, String> dfaStateNames = new HashMap<>();
        Queue<Set<State>> queue = new LinkedList<>();
        AtomicInteger stateCounter = new AtomicInteger(0);

        // Lo stato iniziale del DFA è l'insieme contenente solo lo stato iniziale dell'NFA
        Set<State> initialSuperState = Set.of(nfa.getInitialState());
        String initialName = "D" + stateCounter.getAndIncrement();

        builder.addState(initialName, isFinal(initialSuperState, nfa.getFinalStates()));
        builder.setInitialState(initialName);

        dfaStateNames.put(initialSuperState, initialName);
        queue.add(initialSuperState);

        // Esploriamo tutti i possibili sottoinsiemi
        while (!queue.isEmpty()) {
            Set<State> currentSuperState = queue.poll();
            String currentName = dfaStateNames.get(currentSuperState);

            for (char symbol : alphabet) {
                Set<State> nextSuperState = computeNextSuperState(nfa, currentSuperState, symbol);

                if (nextSuperState.isEmpty()) {
                    continue;
                }

                // Se troviamo un super-stato nuovo, lo registriamo
                String targetName = dfaStateNames.computeIfAbsent(nextSuperState, k -> {
                    String nextName = "D" + stateCounter.getAndIncrement();
                    builder.addState(nextName, isFinal(k, nfa.getFinalStates()));
                    queue.add(k);
                    return nextName;
                });

                builder.addTransition(currentName, symbol, targetName);
            }
        }

        return builder.build();
    }

    /**
     * Metodo di convenienza che applica l'intera catena di trasformazione: ENFA -> NFA -> DFA.
     */
    public static DFA enfaToDfa(ENFA enfa) {
        NFA intermediateNfa = enfaToNfa(enfa);
        return nfaToDfa(intermediateNfa);
    }

    // --- Metodi Helper Interni ---

    /**
     * Calcola il sottoinsieme di stati raggiungibili leggendo un simbolo.
     */
    private static Set<State> computeNextSuperState(NFA nfa, Set<State> currentSuperState, char symbol) {
        Set<State> nextSuperState = new HashSet<>();
        for (State s : currentSuperState) {
            Map<Character, Set<State>> transitions = nfa.getTransitionTable().get(s);
            if (transitions != null && transitions.containsKey(symbol)) {
                nextSuperState.addAll(transitions.get(symbol));
            }
        }
        return nextSuperState;
    }

    /**
     * Calcola i target per un ENFA partendo da una closure e leggendo un simbolo, 
     * applicando la ε-closure al risultato.
     */
    private static Set<State> computeEnfaTargets(ENFA enfa, Set<State> qClosure, char symbol) {
        Set<State> targets = new HashSet<>();
        for (State p : qClosure) {
            Map<Character, Set<State>> transitions = enfa.getTransitionTable().get(p);
            if (transitions != null && transitions.containsKey(symbol)) {
                targets.addAll(enfa.epsilonClosure(transitions.get(symbol)));
            }
        }
        return targets;
    }

    private static Set<Character> getAlphabet(Map<State, Map<Character, Set<State>>> transitionTable) {
        Set<Character> alphabet = new HashSet<>();
        for (Map<Character, Set<State>> transitions : transitionTable.values()) {
            for (Character c : transitions.keySet()) {
                if (c != null) {
                    alphabet.add(c);
                }
            }
        }
        return alphabet;
    }

    private static boolean isFinal(Set<State> superState, Set<State> finalStates) {
        return superState.stream().anyMatch(finalStates::contains);
    }
}