package it.tugamer89.autogex.core;

import java.util.*;

/**
 * Builder astratto basato sul Curiously Recurring Template Pattern (CRTP).
 * Permette di condividere la logica di creazione degli stati tra tutti i Builder
 * specifici (DFA, NFA, ENFA) mantenendo il "method chaining" fluido.
 *
 * @param <B> Il tipo del Builder concreto (es. DFA.Builder)
 * @param <A> Il tipo dell'Automa da costruire (es. DFA)
 */
public abstract class AbstractAutomatonBuilder<B extends AbstractAutomatonBuilder<B, A>, A extends Automaton> {
    
    protected final Map<String, State> states = new HashMap<>();
    protected final Set<State> finalStates = new HashSet<>();
    protected State initialState;

    /**
     * Metodo astratto che ogni Builder concreto deve implementare restituendo 'this'.
     * Serve a garantire che il chaining restituisca il tipo corretto.
     */
    protected abstract B self();

    public B addState(String name, boolean isFinal) {
        State state = new State(name, isFinal);
        states.put(name, state);
        if (isFinal) {
            finalStates.add(state);
        }
        return self();
    }

    public B setInitialState(String name) {
        this.initialState = states.get(name);
        return self();
    }

    /**
     * Esegue i controlli di integrità comuni a tutti i builder.
     */
    protected void validate() {
        if (initialState == null) {
            throw new IllegalStateException("Lo stato iniziale deve essere impostato prima della build.");
        }
    }

    public abstract A build();

    public Map<String, State> getStatesMap() { return states; }
    public Set<State> getFinalStates() { return finalStates; }
    public State getInitialState() { return initialState; }
}