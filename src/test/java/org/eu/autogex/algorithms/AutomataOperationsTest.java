package org.eu.autogex.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eu.autogex.models.DFA;
import org.eu.autogex.regex.Regex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AutomataOperations Set Operations & Closure Properties")
class AutomataOperationsTest {

    /** Builds a complete DFA over {'a', 'b'} accepting strings with an even number of 'a's. */
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

    /** Builds a complete DFA over {'a', 'b'} accepting strings ending with 'b'. */
    private DFA buildEndsWithBDfa() {
        return new DFA.Builder()
                .addState("p0", false)
                .addState("p1", true)
                .setInitialState("p0")
                .addTransition("p0", 'a', "p0")
                .addTransition("p0", 'b', "p1")
                .addTransition("p1", 'a', "p0")
                .addTransition("p1", 'b', "p1")
                .build();
    }

    /**
     * Generates all strings over alphabet {'a', 'b'} up to maxLength for exhaustive equivalence
     * checks.
     */
    private List<String> generateTestStrings(char[] alphabet, int maxLength) {
        List<String> result = new ArrayList<>();
        result.add("");
        List<String> currentLevel = List.of("");
        for (int len = 1; len <= maxLength; len++) {
            List<String> nextLevel = new ArrayList<>();
            for (String prefix : currentLevel) {
                for (char c : alphabet) {
                    String next = prefix + c;
                    nextLevel.add(next);
                    result.add(next);
                }
            }
            currentLevel = nextLevel;
        }
        return result;
    }

    @Nested
    @DisplayName("Complement Operation")
    class ComplementTests {

        @Test
        @DisplayName("Should invert acceptance on a complete DFA")
        void testComplementCompleteDfa() {
            DFA evenAs = buildEvenAsDfa();
            DFA oddAs = AutomataOperations.complement(evenAs);

            for (String s : generateTestStrings(new char[] {'a', 'b'}, 5)) {
                assertEquals(
                        !evenAs.accepts(s), oddAs.accepts(s), "Mismatch on string: '" + s + "'");
            }
        }

        @Test
        @DisplayName("Should complete missing transitions with sink state before complementing")
        void testComplementIncompleteDfa() {
            DFA onlyA =
                    new DFA.Builder()
                            .addState("s0", false)
                            .addState("s1", true)
                            .setInitialState("s0")
                            .addTransition("s0", 'a', "s1")
                            .build();

            DFA notOnlyA = AutomataOperations.complement(onlyA, Set.of('a', 'b'));

            assertFalse(notOnlyA.accepts("a"));
            assertTrue(notOnlyA.accepts(""));
            assertTrue(notOnlyA.accepts("b"));
            assertTrue(notOnlyA.accepts("aa"));
            assertTrue(notOnlyA.accepts("ab"));
            assertTrue(notOnlyA.accepts("ba"));
        }

        @Test
        @DisplayName("Should satisfy double complement identity: L == complement(complement(L))")
        void testDoubleComplementIdentity() {
            DFA endsWithB = buildEndsWithBDfa();
            DFA doubleComplement =
                    AutomataOperations.complement(AutomataOperations.complement(endsWithB));

            for (String s : generateTestStrings(new char[] {'a', 'b'}, 5)) {
                assertEquals(endsWithB.accepts(s), doubleComplement.accepts(s));
            }
        }
    }

    @Nested
    @DisplayName("Product Automaton Operations (Intersection, Union, Difference)")
    class ProductOperationsTests {

        @Test
        @DisplayName("Intersection should accept strings in both L1 and L2")
        void testIntersection() {
            DFA evenAs = buildEvenAsDfa();
            DFA endsWithB = buildEndsWithBDfa();
            DFA intersection = AutomataOperations.intersection(evenAs, endsWithB);

            for (String s : generateTestStrings(new char[] {'a', 'b'}, 5)) {
                boolean expected = evenAs.accepts(s) && endsWithB.accepts(s);
                assertEquals(expected, intersection.accepts(s), "Failed for input: '" + s + "'");
            }
        }

        @Test
        @DisplayName("Union should accept strings in L1 or L2")
        void testUnion() {
            DFA evenAs = buildEvenAsDfa();
            DFA endsWithB = buildEndsWithBDfa();
            DFA union = AutomataOperations.union(evenAs, endsWithB);

            for (String s : generateTestStrings(new char[] {'a', 'b'}, 5)) {
                boolean expected = evenAs.accepts(s) || endsWithB.accepts(s);
                assertEquals(expected, union.accepts(s), "Failed for input: '" + s + "'");
            }
        }

        @Test
        @DisplayName("Difference should accept strings in L1 but not in L2")
        void testDifference() {
            DFA evenAs = buildEvenAsDfa();
            DFA endsWithB = buildEndsWithBDfa();
            DFA difference = AutomataOperations.difference(evenAs, endsWithB);

            for (String s : generateTestStrings(new char[] {'a', 'b'}, 5)) {
                boolean expected = evenAs.accepts(s) && !endsWithB.accepts(s);
                assertEquals(expected, difference.accepts(s), "Failed for input: '" + s + "'");
            }
        }

        @Test
        @DisplayName("Product operations should handle DFAs with disjoint alphabets")
        void testDisjointAlphabets() {
            DFA acceptsA =
                    new DFA.Builder()
                            .addState("q0", false)
                            .addState("q1", true)
                            .setInitialState("q0")
                            .addTransition("q0", 'a', "q1")
                            .build();
            DFA acceptsB =
                    new DFA.Builder()
                            .addState("p0", false)
                            .addState("p1", true)
                            .setInitialState("p0")
                            .addTransition("p0", 'b', "p1")
                            .build();

            DFA union = AutomataOperations.union(acceptsA, acceptsB);
            DFA intersection = AutomataOperations.intersection(acceptsA, acceptsB);
            DFA difference = AutomataOperations.difference(acceptsA, acceptsB);

            assertTrue(union.accepts("a"));
            assertTrue(union.accepts("b"));
            assertFalse(union.accepts("ab"));

            assertFalse(intersection.accepts("a"));
            assertFalse(intersection.accepts("b"));

            assertTrue(difference.accepts("a"));
            assertFalse(difference.accepts("b"));
        }
    }

    @Nested
    @DisplayName("Regular Language Closure Laws Integration Tests")
    class ClosureLawsIntegrationTests {

        @Test
        @DisplayName("De Morgan's Laws hold on DFAs compiled from regular expressions")
        void testDeMorgansLawsFromRegex() {
            Set<Character> alphabet = Set.of('a', 'b');
            DFA dfa1 = new Regex("(a|b)*aa(a|b)*").getAutomaton();
            DFA dfa2 = new Regex("b(a|b)*b").getAutomaton();

            // Law 1: complement(L1 ∪ L2) == complement(L1) ∩ complement(L2)
            DFA lhs1 =
                    AutomataOperations.complement(AutomataOperations.union(dfa1, dfa2), alphabet);
            DFA rhs1 =
                    AutomataOperations.intersection(
                            AutomataOperations.complement(dfa1, alphabet),
                            AutomataOperations.complement(dfa2, alphabet));

            // Law 2: complement(L1 ∩ L2) == complement(L1) ∪ complement(L2)
            DFA lhs2 =
                    AutomataOperations.complement(
                            AutomataOperations.intersection(dfa1, dfa2), alphabet);
            DFA rhs2 =
                    AutomataOperations.union(
                            AutomataOperations.complement(dfa1, alphabet),
                            AutomataOperations.complement(dfa2, alphabet));

            // Difference identity: L1 \ L2 == L1 ∩ complement(L2)
            DFA diffDirect = AutomataOperations.difference(dfa1, dfa2);
            DFA diffViaComplement =
                    AutomataOperations.intersection(
                            dfa1, AutomataOperations.complement(dfa2, alphabet));

            for (String s : generateTestStrings(new char[] {'a', 'b'}, 5)) {
                assertEquals(
                        lhs1.accepts(s), rhs1.accepts(s), "De Morgan 1 failed on: '" + s + "'");
                assertEquals(
                        lhs2.accepts(s), rhs2.accepts(s), "De Morgan 2 failed on: '" + s + "'");
                assertEquals(
                        diffDirect.accepts(s),
                        diffViaComplement.accepts(s),
                        "Difference identity failed on: '" + s + "'");
            }
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should throw NullPointerException on null arguments")
        void testNullArguments() {
            DFA valid = buildEvenAsDfa();

            assertThrows(NullPointerException.class, () -> AutomataOperations.complement(null));
            assertThrows(
                    NullPointerException.class, () -> AutomataOperations.complement(valid, null));
            assertThrows(
                    NullPointerException.class, () -> AutomataOperations.intersection(null, valid));
            assertThrows(
                    NullPointerException.class, () -> AutomataOperations.intersection(valid, null));
            assertThrows(NullPointerException.class, () -> AutomataOperations.union(null, valid));
            assertThrows(NullPointerException.class, () -> AutomataOperations.union(valid, null));
            assertThrows(
                    NullPointerException.class, () -> AutomataOperations.difference(null, valid));
            assertThrows(
                    NullPointerException.class, () -> AutomataOperations.difference(valid, null));
        }
    }
}
