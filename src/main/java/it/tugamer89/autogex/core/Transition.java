package it.tugamer89.autogex.core;

/**
 * Rappresenta una singola transizione da uno stato a un altro leggendo un simbolo.
 */
public record Transition(State from, Character symbol, State to) {

    /**
     * Controlla se questa è una transizione silente.
     * Usiamo 'null' per rappresentare epsilon.
     */
    public boolean isEpsilon() {
        return symbol == null;
    }

    @Override
    public String toString() {
        String symStr = isEpsilon() ? "ε" : symbol.toString();
        return from.getName() + " --" + symStr + "--> " + to.getName();
    }
}