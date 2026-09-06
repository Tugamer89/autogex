package org.eu.autogex.algorithms;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eu.autogex.models.NFA;
import org.eu.autogex.regex.Regex;
import org.junit.jupiter.api.Test;

class ConverterSecurityTest {

    @Test
    void testNfaToDfaStateExplosion() {
        NFA.Builder builder = new NFA.Builder();
        int k = 14;

        for (int i = 0; i <= k; i++) {
            builder.addState("q" + i, i == k);
        }
        builder.setInitialState("q0");

        builder.addTransition("q0", '0', "q0");
        builder.addTransition("q0", '1', "q0");
        builder.addTransition("q0", '1', "q1");

        for (int i = 1; i < k; i++) {
            builder.addTransition("q" + i, '0', "q" + (i + 1));
            builder.addTransition("q" + i, '1', "q" + (i + 1));
        }

        NFA nfa = builder.build();

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> Converter.nfaToDfa(nfa));
        assertTrue(
                exception.getMessage().contains("DFA state limit exceeded"),
                "Should detect state explosion to prevent DoS directly in nfaToDfa");
    }

    @Test
    void testStateExplosionDoSPrevention() {
        // Construct a pattern known to cause exponential blow-up during subset construction
        StringBuilder sb = new StringBuilder("(a|b)*a");
        for (int i = 0; i < 20; i++) {
            sb.append("(a|b)");
        }

        String pattern = sb.toString();
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> new Regex(pattern));
        assertTrue(
                exception.getMessage().contains("DFA state limit exceeded"),
                "Should detect state explosion to prevent DoS");
    }
}
