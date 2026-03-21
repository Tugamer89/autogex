package it.tugamer89.autogex.algorithms;

import it.tugamer89.autogex.core.State;
import it.tugamer89.autogex.models.DFA;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility per la minimizzazione di un DFA.
 * Implementa l'algoritmo di raffinamento delle partizioni di Moore.
 */
public class Minimizer {

    private Minimizer() {
        throw new UnsupportedOperationException("Classe di utility");
    }

    /**
     * Minimizza un DFA restituendo un nuovo DFA equivalente con il numero minimo di stati.
     */
    public static DFA minimize(DFA dfa) {
        // 1. Rimuoviamo eventuali stati irraggiungibili
        Set<State> reachableStates = getReachableStates(dfa);
        Set<Character> alphabet = getAlphabet(dfa);

        // 2. Partizione iniziale (Finali vs Non Finali)
        Set<Set<State>> partitions = createInitialPartitions(dfa, reachableStates);

        // 3. Raffinamento iterativo delle partizioni (Punto Fisso)
        boolean changed = true;
        while (changed) {
            changed = false;
            Set<Set<State>> newPartitions = new HashSet<>();

            for (Set<State> group : partitions) {
                // Suddivide il gruppo in base al comportamento (destinazioni delle transizioni)
                Map<Map<Character, Set<State>>, Set<State>> subGroups = splitGroup(dfa, group, alphabet, partitions);
                
                newPartitions.addAll(subGroups.values());
                
                // Se un gruppo è stato spezzato in 2 o più sottogruppi, la partizione è cambiata
                if (subGroups.size() > 1) {
                    changed = true;
                }
            }
            partitions = newPartitions;
        }

        // 4. Ricostruzione del DFA Minimizzato
        return buildMinimalDfa(dfa, partitions, alphabet);
    }

    // --- Metodi Helper ---

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
        
        // Mappa la "firma" comportamentale di uno stato al sottogruppo di stati che la condividono
        Map<Map<Character, Set<State>>, Set<State>> subGroups = new HashMap<>();

        for (State s : group) {
            // La firma è: "Per ogni carattere, in quale partizione finisco?"
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

        // Registrazione dei nuovi stati
        for (Set<State> partition : partitions) {
            String name = "M" + counter.getAndIncrement();
            partitionToName.put(partition, name);
            
            // La partizione è finale se contiene almeno uno stato finale originale
            boolean isFinal = partition.stream().anyMatch(s -> originalDfa.getFinalStates().contains(s));
            builder.addState(name, isFinal);

            // La partizione è iniziale se contiene lo stato iniziale originale
            if (partition.contains(originalDfa.getInitialState())) {
                builder.setInitialState(name);
            }
        }

        // Creazione delle transizioni (basta prelevare un elemento "rappresentante" per ogni partizione)
        for (Set<State> partition : partitions) {
            State representative = partition.iterator().next();
            String currentName = partitionToName.get(partition);

            for (char symbol : alphabet) {
                State dest = getDestination(originalDfa, representative, symbol);
                Set<State> targetPartition = findPartitionContaining(partitions, dest);
                
                // Se c'è una transizione valida, la colleghiamo
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