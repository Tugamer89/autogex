package org.eu.autogex.export;

import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class GraphvizExporterTest {

    @Test
    void testExportDFA() {
        DFA dfa = new DFA.Builder()
                .addState("q0", false)
                .addState("q1", true)
                .setInitialState("q0")
                .addTransition("q0", 'a', "q1")
                .build();

        String dot = GraphvizExporter.toDot(dfa);

        assertTrue(dot.startsWith("digraph Automaton {"), "Must start with digraph declaration");
        assertTrue(dot.contains("rankdir=LR;"), "Must specify Left-to-Right orientation");
        assertTrue(dot.contains("node [shape = doublecircle]; \"q1\";"), "q1 must be a double circle (final state)");
        assertTrue(dot.contains("__start0 -> \"q0\";"), "Must have an entry arrow to q0");
        assertTrue(dot.contains("\"q0\" -> \"q1\" [label=\"a\"];"), "Must contain the transition from q0 to q1");
        assertTrue(dot.endsWith("}\n"), "Must close the digraph properly");
    }

    @Test
    void testExportNFA() {
        NFA nfa = new NFA.Builder()
                .addState("start", false)
                .addState("end", true)
                .setInitialState("start")
                .addTransition("start", '0', "end")
                .addTransition("start", '0', "start") // NFA branch
                .build();

        String dot = GraphvizExporter.toDot(nfa);

        assertTrue(dot.contains("node [shape = doublecircle]; \"end\";"));
        assertTrue(dot.contains("\"start\" -> \"end\" [label=\"0\"];"));
        assertTrue(dot.contains("\"start\" -> \"start\" [label=\"0\"];"));
    }

    @Test
    void testExportENFAWithEpsilon() {
        ENFA enfa = new ENFA.Builder()
                .addState("A", false)
                .addState("B", true)
                .setInitialState("A")
                .addEpsilonTransition("A", "B")
                .build();

        String dot = GraphvizExporter.toDot(enfa);

        assertTrue(dot.contains("node [shape = doublecircle]; \"B\";"));
        assertTrue(dot.contains("\"A\" -> \"B\" [label=\"ε\"];"), "Epsilon transitions must be labeled with ε");
    }

    @Test
    void testUtilityClassConstructorThrowsException() throws Exception {
        Constructor<GraphvizExporter> constructor = GraphvizExporter.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }
}