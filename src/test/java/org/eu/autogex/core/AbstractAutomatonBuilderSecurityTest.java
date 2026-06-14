package org.eu.autogex.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AbstractAutomatonBuilderSecurityTest {

    static class TestBuilder extends AbstractAutomatonBuilder<TestBuilder, Automaton> {
        @Override
        protected TestBuilder self() {
            return this;
        }

        @Override
        public Automaton build() {
            return null;
        }
    }

    @Test
    void testStateExplosionDoSPreventionInBuilder() {
        TestBuilder builder = new TestBuilder();
        for (int i = 0; i < 10000; i++) {
            builder.addState("q" + i, false);
        }

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class, () -> builder.addState("overflow", false));
        assertTrue(
                exception.getMessage().contains("Builder state limit exceeded"),
                "Should detect state explosion to prevent DoS during manual construction");
    }
}
