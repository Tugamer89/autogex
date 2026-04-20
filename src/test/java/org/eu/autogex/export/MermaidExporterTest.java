package org.eu.autogex.export;

import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class MermaidExporterTest {

    @Test
    void testExportDFA() {
        DFA dfa = new DFA.Builder()
                .addState("q 0", false) // Testing space in name
                .addState("q1", true)
                .setInitialState("q 0")
                .addTransition("q 0", 'a', "q1")
                .build();

        String mermaid = MermaidExporter.toMermaid(dfa);

        assertTrue(mermaid.startsWith("stateDiagram-v2"), "Must start with stateDiagram-v2 declaration");
        assertTrue(mermaid.contains("direction LR"), "Must specify Left-to-Right orientation");
        assertTrue(mermaid.contains("state \"q 0\" as s_q_0"), "Must alias state names with spaces securely");
        assertTrue(mermaid.contains("[*] --> s_q_0"), "Must have an entry arrow to the initial state");
        assertTrue(mermaid.contains("s_q1 --> [*]"), "Must have an exit arrow for the final state");
        assertTrue(mermaid.contains("s_q_0 --> s_q1 : a"), "Must contain the transition from initial to final with label 'a'");
    }

    @Test
    void testExportNFA() {
        NFA nfa = new NFA.Builder()
                .addState("start", false)
                .addState("end", true)
                .setInitialState("start")
                .addTransition("start", '0', "end")
                .addTransition("start", '0', "start") // NFA branching
                .build();

        String mermaid = MermaidExporter.toMermaid(nfa);

        assertTrue(mermaid.contains("s_end --> [*]"));
        assertTrue(mermaid.contains("s_start --> s_end : 0"));
        assertTrue(mermaid.contains("s_start --> s_start : 0"));
    }

    @Test
    void testExportENFAWithEpsilon() {
        ENFA enfa = new ENFA.Builder()
                .addState("A", false)
                .addState("B", true)
                .setInitialState("A")
                .addEpsilonTransition("A", "B")
                .build();

        String mermaid = MermaidExporter.toMermaid(enfa);

        assertTrue(mermaid.contains("s_B --> [*]"));
        assertTrue(mermaid.contains("s_A --> s_B : ε"), "Epsilon transitions must be labeled with ε");
    }

    @Test
    void testUtilityClassConstructorThrowsException() throws Exception {
        Constructor<MermaidExporter> constructor = MermaidExporter.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }
}