package org.eu.autogex.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.eu.autogex.models.DFA;
import org.eu.autogex.regex.Regex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LanguageGenerator Fuzzing & Lexicographical Enumeration")
class LanguageGeneratorTest {

    /** Builds a cyclic DFA over {'a', 'b'} accepting strings with an even number of 'a's. */
    private DFA buildEvenAsDfa() {
        return new DFA.Builder()
                .addState("q0", true)
                .addState("q1", false)
                .setInitialState("q0")
                .addTransition("q0", 'a', "q1")
                .addTransition("q0", 'b', "q0")
                .addTransition("q1", 'a', "q0")
                .addTransition("q1", 'b', "q1")
                .build();
    }

    /**
     * Builds a cyclic DFA with a dead-end sink trap cycle ('q_dead') that accepts only "ab"
     * followed by zero or more 'c's.
     */
    private DFA buildCyclicWithSinkTrapDfa() {
        return new DFA.Builder()
                .addState("q0", false)
                .addState("q1", false)
                .addState("q2", true)
                .addState("q_dead", false)
                .setInitialState("q0")
                .addTransition("q0", 'a', "q1")
                .addTransition("q0", 'b', "q_dead")
                .addTransition("q1", 'b', "q2")
                .addTransition("q1", 'a', "q_dead")
                .addTransition("q2", 'c', "q2")
                .addTransition("q2", 'a', "q_dead")
                .addTransition("q2", 'b', "q_dead")
                .addTransition("q_dead", 'a', "q_dead")
                .addTransition("q_dead", 'b', "q_dead")
                .addTransition("q_dead", 'c', "q_dead")
                .build();
    }

    @Nested
    @DisplayName("Random Generation (Fuzzing)")
    class RandomGenerationTests {

        @Test
        @DisplayName("Should generate 100% valid strings across cyclic DFAs")
        void testGenerateRandomValidityOnCyclicDfa() {
            DFA dfa = new Regex("(a|b)*abb").getAutomaton();
            Random seeded = new Random(42L);

            for (int i = 0; i < 200; i++) {
                String generated = LanguageGenerator.generateRandom(dfa, 12, seeded);
                assertTrue(generated.length() <= 12, "Exceeded maxLength: " + generated);
                assertTrue(dfa.accepts(generated), "DFA rejected generated string: " + generated);
            }
        }

        @Test
        @DisplayName("Should avoid dead-end sink cycles and always terminate with valid strings")
        void testGenerateRandomAvoidsSinkCycles() {
            DFA trapDfa = buildCyclicWithSinkTrapDfa();
            Random seeded = new Random(12345L);

            for (int i = 0; i < 100; i++) {
                String generated = LanguageGenerator.generateRandom(trapDfa, 10, seeded);
                assertTrue(generated.startsWith("ab"));
                assertTrue(generated.length() <= 10);
                assertTrue(trapDfa.accepts(generated));
            }
        }

        @Test
        @DisplayName("Should handle maxLength = 0 when empty string is accepted")
        void testGenerateRandomZeroLength() {
            DFA evenAs = buildEvenAsDfa();
            assertEquals("", LanguageGenerator.generateRandom(evenAs, 0));
        }

        @Test
        @DisplayName("Should throw when no accepted string exists within maxLength")
        void testGenerateRandomImpossibleLength() {
            DFA trapDfa = buildCyclicWithSinkTrapDfa(); // shortest accepted string is "ab" (len 2)
            assertThrows(
                    IllegalArgumentException.class,
                    () -> LanguageGenerator.generateRandom(trapDfa, 1));
        }
    }

    @Nested
    @DisplayName("Lexicographical Generation (Bounded BFS)")
    class LexicographicalGenerationTests {

        @Test
        @DisplayName("Should yield all accepted strings up to N in canonical lexicographical order")
        void testGenerateAllLexicographicalOrder() {
            DFA evenAs = buildEvenAsDfa();

            List<String> generated = LanguageGenerator.generateAll(evenAs, 3);

            List<String> expected = List.of("", "b", "aa", "bb", "aab", "aba", "baa", "bbb");

            assertEquals(expected, generated);
            for (String s : generated) {
                assertTrue(evenAs.accepts(s), "DFA rejected enumerated string: '" + s + "'");
            }
        }

        @Test
        @DisplayName("Should terminate cleanly on cyclic DFAs with sink traps")
        void testGenerateAllWithSinkCycle() {
            DFA trapDfa = buildCyclicWithSinkTrapDfa();

            List<String> generated = LanguageGenerator.generateAll(trapDfa, 5);

            assertEquals(List.of("ab", "abc", "abcc", "abccc"), generated);
            generated.forEach(s -> assertTrue(trapDfa.accepts(s)));
        }

        @Test
        @DisplayName(
                "Should return only empty string when maxLength = 0 and initial state is final")
        void testGenerateAllZeroLengthAccepting() {
            DFA evenAs = buildEvenAsDfa();
            assertEquals(List.of(""), LanguageGenerator.generateAll(evenAs, 0));
        }

        @Test
        @DisplayName("Should return empty list when DFA has no accepting states")
        void testGenerateAllEmptyLanguage() {
            DFA emptyLangDfa =
                    new DFA.Builder()
                            .addState("q0", false)
                            .setInitialState("q0")
                            .addTransition("q0", 'a', "q0")
                            .build();

            List<String> generated = LanguageGenerator.generateAll(emptyLangDfa, 5);
            assertTrue(generated.isEmpty());
            assertFalse(emptyLangDfa.accepts(""));
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should validate null DFA, null RandomGenerator, and negative maxLength")
        void testInvalidArguments() {
            DFA valid = buildEvenAsDfa();

            assertThrows(
                    NullPointerException.class, () -> LanguageGenerator.generateRandom(null, 5));
            assertThrows(
                    NullPointerException.class,
                    () -> LanguageGenerator.generateRandom(valid, 5, null));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> LanguageGenerator.generateRandom(valid, -1));

            assertThrows(NullPointerException.class, () -> LanguageGenerator.generateAll(null, 5));
            assertThrows(
                    IllegalArgumentException.class, () -> LanguageGenerator.generateAll(valid, -1));
        }
    }
}
