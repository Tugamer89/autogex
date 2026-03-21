package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.AbstractAutomaton;
import it.tugamer89.autogex.core.AbstractAutomatonBuilder;
import it.tugamer89.autogex.core.State;

import java.util.*;

/**
 * Automa a Stati Finiti Non Deterministico (NFA).
 */
public class NFA extends AbstractAutomaton {
    
    // Mappa: Stato Partenza -> (Mappa: Carattere -> Insieme di Stati di Arrivo)
    private final Map<State, Map<Character, Set<State>>> transitionTable;

    private NFA(Builder builder) {
        super(builder);
        this.transitionTable = Map.copyOf(builder.transitionTable);
    }

    @Override
    public boolean accepts(String input) {
        // L'NFA può trovarsi in più stati contemporaneamente
        Set<State> currentStates = new HashSet<>();
        currentStates.add(initialState);
        
        for (char symbol : input.toCharArray()) {
            Set<State> nextStates = new HashSet<>();
            
            for (State state : currentStates) {
                Map<Character, Set<State>> stateTransitions = transitionTable.get(state);
                if (stateTransitions != null && stateTransitions.containsKey(symbol)) {
                    nextStates.addAll(stateTransitions.get(symbol));
                }
            }
            
            currentStates = nextStates;
            
            // Ottimizzazione: se non ci sono più stati attivi, la stringa è rifiutata
            if (currentStates.isEmpty()) {
                return false;
            }
        }
        
        // Accetta se almeno uno degli stati correnti finali è uno stato finale dell'NFA
        return currentStates.stream().anyMatch(finalStates::contains);
    }

    public Map<State, Map<Character, Set<State>>> getTransitionTable() {
        return transitionTable;
    }

    /**
     * Pattern Builder per costruire l'NFA in modo fluente.
     */
    public static class Builder extends AbstractAutomatonBuilder<Builder, NFA> {
        
        private final Map<State, Map<Character, Set<State>>> transitionTable = new HashMap<>();

        @Override
        protected Builder self() {
            return this;
        }

        public Builder addTransition(String fromName, char symbol, String toName) {
            State from = states.get(fromName);
            State to = states.get(toName);
            
            if (from == null || to == null) {
                throw new IllegalArgumentException("Stato non trovato. Aggiungilo prima con addState.");
            }

            transitionTable.computeIfAbsent(from, k -> new HashMap<>())
                           .computeIfAbsent(symbol, k -> new HashSet<>())
                           .add(to);
            return self();
        }

        @Override
        public NFA build() {
            validate();
            return new NFA(this);
        }
    }
}