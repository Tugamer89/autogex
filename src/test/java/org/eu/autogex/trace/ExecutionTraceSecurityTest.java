package org.eu.autogex.trace;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eu.autogex.models.DFA;
import org.junit.jupiter.api.Test;

class ExecutionTraceSecurityTest {

    @Test
    void testExecutionTraceInputLengthLimitDoSPrevention() {
        DFA dfa =
                new DFA.Builder()
                        .addState("q0", true)
                        .setInitialState("q0")
                        .addTransition("q0", 'a', "q0")
                        .build();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ExecutionTrace.MAX_TRACE_INPUT_LENGTH + 1; i++) {
            sb.append('a');
        }
        String payload = sb.toString();

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> dfa.execute(payload));
        assertTrue(
                exception.getMessage().contains("too large for tracing (Security: DoS prevention)"),
                "Should detect oversized input string to prevent DoS via memory exhaustion");
    }
}
