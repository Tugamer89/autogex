package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.AbstractAutomaton;
import it.tugamer89.autogex.core.AbstractAutomatonBuilder;
import it.tugamer89.autogex.core.State;

import java.util.*;

/**
 * Automa a Stati Finiti Non Deterministico con Epsilon Transizioni (ε-NFA).
 */
public class ENFA extends AbstractAutomaton {
    
    // Il carattere 'null' viene usato per rappresentare la ε-transizione
    private final Map<State, Map<Character, Set<State>>> transitionTable;

    private ENFA(Builder builder) {
        super(builder);
        this.transitionTable = Map.copyOf(builder.transitionTable);
    }

    /**
     * Calcola la ε-closure di un insieme di stati.
     * (Tutti gli stati raggiungibili senza consumare input).
     */
    public Set<State> epsilonClosure(Set<State> startStates) {
        Set<State> closure = new HashSet<>(startStates);
        Queue<State> queue = new LinkedList<>(startStates);
        
        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            Map<Character, Set<State>> stateTransitions = transitionTable.get(currentState);
            
            // Cerca le transizioni associate a null (ε)
            if (stateTransitions != null && stateTransitions.containsKey(null)) {
                for (State nextState : stateTransitions.get(null)) {
                    // Se non lo abbiamo ancora visitato, lo aggiungiamo alla closure e alla coda
                    if (closure.add(nextState)) {
                        queue.add(nextState);
                    }
                }
            }
        }
        return closure;
    }

    @Override
    public boolean accepts(String input) {
        // Partiamo dalla ε-closure dello stato iniziale
        Set<State> currentStates = epsilonClosure(Set.of(initialState));
        
        for (char symbol : input.toCharArray()) {
            Set<State> nextStates = new HashSet<>();
            
            for (State state : currentStates) {
                Map<Character, Set<State>> stateTransitions = transitionTable.get(state);
                if (stateTransitions != null && stateTransitions.containsKey(symbol)) {
                    nextStates.addAll(stateTransitions.get(symbol));
                }
            }
            
            // Dopo aver letto il simbolo, espandiamo con la ε-closure
            currentStates = epsilonClosure(nextStates);
            
            if (currentStates.isEmpty()) {
                return false;
            }
        }
        
        return currentStates.stream().anyMatch(finalStates::contains);
    }

    public Map<State, Map<Character, Set<State>>> getTransitionTable() {
        return transitionTable;
    }

    /**
     * Pattern Builder per costruire l'ENFA in modo fluente.
     */
    public static class Builder extends AbstractAutomatonBuilder<Builder, ENFA> {
        
        private final Map<State, Map<Character, Set<State>>> transitionTable = new HashMap<>();

        @Override
        protected Builder self() {
            return this;
        }

        public Builder addTransition(String fromName, Character symbol, String toName) {
            State from = states.get(fromName);
            State to = states.get(toName);
            
            if (from == null || to == null) {
                throw new IllegalArgumentException("Stato non trovato. Aggiungilo prima con addState.");
            }

            transitionTable.computeIfAbsent(from, k -> new HashMap<>())
                           .computeIfAbsent(symbol, k -> new HashSet<>())
                           .add(to);
            return this;
        }

        // Metodo di utilità per rendere il codice più leggibile per le transizioni silenti
        public Builder addEpsilonTransition(String fromName, String toName) {
            return addTransition(fromName, null, toName);
        }

        @Override
        public ENFA build() {
            validate();
            return new ENFA(this);
        }
    }
}