package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.AbstractAutomaton;
import it.tugamer89.autogex.core.AbstractAutomatonBuilder;
import it.tugamer89.autogex.core.State;

import java.util.*;

/**
 * Automa a Stati Finiti Deterministico (DFA).
 */
public class DFA extends AbstractAutomaton {
    
    // Mappa: Stato Partenza -> (Mappa: Carattere -> Stato Arrivo)
    private final Map<State, Map<Character, State>> transitionTable;

    private DFA(Builder builder) {
        super(builder);
        this.transitionTable = Map.copyOf(builder.transitionTable);
    }

    @Override
    public boolean accepts(String input) {
        State currentState = initialState;
        
        for (char symbol : input.toCharArray()) {
            Map<Character, State> stateTransitions = transitionTable.get(currentState);
            
            // Se non c'è una transizione definita per questo carattere, la stringa viene rifiutata
            if (stateTransitions == null || !stateTransitions.containsKey(symbol)) {
                return false;
            }
            
            currentState = stateTransitions.get(symbol);
        }
        
        // La stringa è accettata se e solo se, alla fine, mi trovo in uno stato finale
        return finalStates.contains(currentState);
    }

    public Map<State, Map<Character, State>> getTransitionTable() {
        return transitionTable;
    }

    /**
     * Pattern Builder per costruire il DFA in modo fluente.
     */
    public static class Builder extends AbstractAutomatonBuilder<Builder, DFA> {
        
        private final Map<State, Map<Character, State>> transitionTable = new HashMap<>();

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

            transitionTable.computeIfAbsent(from, k -> new HashMap<>()).put(symbol, to);
            return self();
        }

        @Override
        public DFA build() {
            validate();
            return new DFA(this);
        }
    }
}