package org.eu.autogex.algorithms;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eu.autogex.regex.Regex;
import org.junit.jupiter.api.Test;

class ConverterSecurityTest {

    @Test
    void testStateExplosionDoSPrevention() {
        // Construct a pattern known to cause exponential blow-up during subset construction
        StringBuilder sb = new StringBuilder("(a|b)*a");
        for (int i = 0; i < 20; i++) {
            sb.append("(a|b)");
        }

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> new Regex(sb.toString()));
        assertTrue(
                exception.getMessage().contains("DFA state limit exceeded"),
                "Should detect state explosion to prevent DoS");
    }
}
