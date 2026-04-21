package org.eu.autogex.trace;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.eu.autogex.models.DFA;
import org.junit.jupiter.api.Test;

class ExecutionTraceTest {

    @Test
    void testExecutionTraceGeneration() {
        // Build a simple DFA
        DFA dfa =
                new DFA.Builder()
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
        assertTrue(
                formattedOutput.contains("REJECTED"),
                "Formatted output should indicate acceptance status");
        assertTrue(
                formattedOutput.contains("--[a]--> {q1}"),
                "Formatted output should show transition for 'a'");
        assertTrue(
                formattedOutput.contains("TRACE FOR INPUT: 'ab'"),
                "Formatted output should show the input trace");
    }

    @Test
    void testExecutionTraceGenerationAccepted() {
        // Let's build the same simple DFA
        DFA dfa =
                new DFA.Builder()
                        .addState("q0", false)
                        .addState("q1", true)
                        .setInitialState("q0")
                        .addTransition("q0", 'a', "q1")
                        .addTransition("q1", 'b', "q0")
                        .build();

        // Let's execute a string that is accepted (ends in q1)
        ExecutionTrace trace = dfa.execute("a");

        assertTrue(trace.isAccepted(), "The string 'a' ends in q1, which is a final state");

        // Test the positive outcome branch
        String formattedOutput = trace.getFormattedTrace();
        assertTrue(
                formattedOutput.contains("ACCEPTED"),
                "The formatted output should indicate ACCEPTED if the string is accepted");
    }

    @Test
    void testExecutionStepEmptyAndNullStates() {
        // Test for the branch where "states == null" in formatStates
        ExecutionStep stepNull = new ExecutionStep(null, 'a', null);
        assertEquals(
                "∅ --[a]--> ∅",
                stepNull.toString(),
                "The null sets should be formatted as ∅");

        // Test for the branch where "states.isEmpty()" in formatStates
        ExecutionStep stepEmpty =
                new ExecutionStep(Collections.emptySet(), 'b', Collections.emptySet());
        assertEquals(
                "∅ --[b]--> ∅",
                stepEmpty.toString(),
                "The empty sets should be formatted as ∅");

        // Test combined (Epsilon + empty sets)
        ExecutionStep stepEpsilonEmpty =
                new ExecutionStep(Collections.emptySet(), null, Collections.emptySet());
        assertEquals(
                "∅ --[ε]--> ∅",
                stepEpsilonEmpty.toString(),
                "The epsilon with empty sets should be handled correctly");
    }
}
