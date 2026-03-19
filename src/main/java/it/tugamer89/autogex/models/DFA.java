package it.tugamer89.autogex.models;

import it.tugamer89.autogex.core.Automaton;
import it.tugamer89.autogex.core.State;

import java.util.*;

/**
 * Automa a Stati Finiti Deterministico (DFA).
 */
public class DFA implements Automaton {
    private final Set<State> states;
    private final State initialState;
    private final Set<State> finalStates;
    
    // Funzione di transizione: δ(q, σ) = q'
    // Mappa: Stato Partenza -> (Mappa: Carattere -> Stato Arrivo)
    private final Map<State, Map<Character, State>> transitionTable;

    private DFA(Builder builder) {
        this.states = Set.copyOf(builder.states.values());
        this.initialState = builder.initialState;
        this.finalStates = Set.copyOf(builder.finalStates);
        this.transitionTable = Map.copyOf(builder.transitionTable);
    }

    @Override
    public Set<State> getStates() { return states; }

    @Override
    public State getInitialState() { return initialState; }

    @Override
    public Set<State> getFinalStates() { return finalStates; }

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

    /**
     * Pattern Builder per costruire il DFA in modo fluente.
     */
    public static class Builder {
        private final Map<String, State> states = new HashMap<>();
        private final Set<State> finalStates = new HashSet<>();
        private final Map<State, Map<Character, State>> transitionTable = new HashMap<>();
        private State initialState;

        public Builder addState(String name, boolean isFinal) {
            State state = new State(name, isFinal);
            states.put(name, state);
            if (isFinal) {
                finalStates.add(state);
            }
            return this;
        }

        public Builder setInitialState(String name) {
            this.initialState = states.get(name);
            return this;
        }

        public Builder addTransition(String fromName, char symbol, String toName) {
            State from = states.get(fromName);
            State to = states.get(toName);
            
            if (from == null || to == null) {
                throw new IllegalArgumentException("Stato non trovato. Aggiungilo prima con addState.");
            }

            transitionTable.computeIfAbsent(from, k -> new HashMap<>()).put(symbol, to);
            return this;
        }

        public DFA build() {
            if (initialState == null) {
                throw new IllegalStateException("Lo stato iniziale deve essere impostato.");
            }
            return new DFA(this);
        }
    }
}