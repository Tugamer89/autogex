package org.eu.autogex.trace;

import org.eu.autogex.models.DFA;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionTraceTest {

    @Test
    void testExecutionTraceGeneration() {
        // Build a simple DFA
        DFA dfa = new DFA.Builder()
                .addState("q0", false)
                .addState("q1", true)
                .setInitialState("q0")
                .addTransition("q0", 'a', "q1")
                .addTransition("q1", 'b', "q0")
                .build();

        // Perform execution
        ExecutionTrace trace = dfa.execute("ab");

        // Validate basic properties
        assertEquals("ab", trace.getInput());
        assertFalse(trace.isAccepted(), "The string 'ab' ends in q0, which is not final");
        
        // Validate step sequence
        assertEquals(3, trace.getSteps().size(), "There should be 1 setup step + 2 reading steps");

        ExecutionStep step1 = trace.getSteps().get(1);
        assertEquals('a', step1.symbolRead());
        assertTrue(step1.toStates().stream().anyMatch(s -> s.getName().equals("q1")));

        // Validate formatting logic (Rejected branch)
        String formattedOutput = trace.getFormattedTrace();
        assertTrue(formattedOutput.contains("REJECTED"), "Formatted output should indicate acceptance status");
        assertTrue(formattedOutput.contains("--[a]--> {q1}"), "Formatted output should show transition for 'a'");
        assertTrue(formattedOutput.contains("TRACE FOR INPUT: 'ab'"), "Formatted output should show the input trace");
    }

    @Test
    void testExecutionTraceGenerationAccepted() {
        // Costruiamo lo stesso DFA semplice
        DFA dfa = new DFA.Builder()
                .addState("q0", false)
                .addState("q1", true)
                .setInitialState("q0")
                .addTransition("q0", 'a', "q1")
                .addTransition("q1", 'b', "q0")
                .build();

        // Eseguiamo una stringa che viene accettata (termina in q1)
        ExecutionTrace trace = dfa.execute("a");

        assertTrue(trace.isAccepted(), "La stringa 'a' termina in q1, che è uno stato finale");

        // Testiamo il branch dell'esito positivo
        String formattedOutput = trace.getFormattedTrace();
        assertTrue(formattedOutput.contains("ACCEPTED"), "L'output formattato deve indicare ACCEPTED se la stringa è accettata");
    }

    @Test
    void testExecutionStepEmptyAndNullStates() {
        // Test per il branch in cui "states == null" in formatStates
        ExecutionStep stepNull = new ExecutionStep(null, 'a', null);
        assertEquals("∅ --[a]--> ∅", stepNull.toString(), "Gli insiemi nulli devono essere formattati come ∅");

        // Test per il branch in cui "states.isEmpty()" in formatStates
        ExecutionStep stepEmpty = new ExecutionStep(Collections.emptySet(), 'b', Collections.emptySet());
        assertEquals("∅ --[b]--> ∅", stepEmpty.toString(), "Gli insiemi vuoti devono essere formattati come ∅");

        // Test combinato (Epsilon + insiemi vuoti)
        ExecutionStep stepEpsilonEmpty = new ExecutionStep(Collections.emptySet(), null, Collections.emptySet());
        assertEquals("∅ --[ε]--> ∅", stepEpsilonEmpty.toString(), "Gli epsilon con insiemi vuoti devono essere gestiti correttamente");
    }
}