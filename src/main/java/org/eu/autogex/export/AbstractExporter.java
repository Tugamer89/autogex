package org.eu.autogex.export;

import java.util.Map;
import java.util.Set;
import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;

/** Base class for exporters providing common traversal logic over automata transitions. */
abstract class AbstractExporter {

    protected static final String EPSILON_LABEL = "ε";

    protected AbstractExporter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** Functional interface for processing a single transition. */
    @FunctionalInterface
    protected interface TransitionConsumer {
        void accept(State source, String label, State target);
    }

    protected static void traverseTransitions(DFA dfa, TransitionConsumer consumer) {
        for (Map.Entry<State, Map<Character, State>> entry : dfa.getTransitionTable().entrySet()) {
            State source = entry.getKey();
            for (Map.Entry<Character, State> transition : entry.getValue().entrySet()) {
                consumer.accept(source, transition.getKey().toString(), transition.getValue());
            }
        }
    }

    protected static void traverseTransitions(NFA nfa, TransitionConsumer consumer) {
        for (Map.Entry<State, Map<Character, Set<State>>> entry :
                nfa.getTransitionTable().entrySet()) {
            State source = entry.getKey();
            for (Map.Entry<Character, Set<State>> transition : entry.getValue().entrySet()) {
                String label = transition.getKey().toString();
                for (State target : transition.getValue()) {
                    consumer.accept(source, label, target);
                }
            }
        }
    }

    protected static void traverseTransitions(ENFA enfa, TransitionConsumer consumer) {
        for (Map.Entry<State, Map<Character, Set<State>>> entry :
                enfa.getTransitionTable().entrySet()) {
            State source = entry.getKey();
            for (Map.Entry<Character, Set<State>> transition : entry.getValue().entrySet()) {
                String label =
                        transition.getKey() == null
                                ? EPSILON_LABEL
                                : transition.getKey().toString();
                for (State target : transition.getValue()) {
                    consumer.accept(source, label, target);
                }
            }
        }
    }
}
