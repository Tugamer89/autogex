package org.eu.autogex.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.eu.autogex.algorithms.AutomataOperations;
import org.eu.autogex.algorithms.LanguageGenerator;
import org.eu.autogex.core.Automaton;
import org.eu.autogex.core.State;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;
import org.eu.autogex.regex.Regex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AutomatonSerializer JSON Serialization")
class AutomatonSerializerTest {

    @Test
    @DisplayName("Should serialize a DFA into canonical deterministic JSON")
    void testSerializeDFA() {
        DFA dfa =
                new DFA.Builder()
                        .addState("q0", false)
                        .addState("q1", true)
                        .setInitialState("q0")
                        .addTransition("q0", 'a', "q1")
                        .addTransition("q1", 'b', "q0")
                        .build();

        String expected =
                """
                {
                  "type": "DFA",
                  "alphabet": [
                    "a",
                    "b"
                  ],
                  "states": [
                    "q0",
                    "q1"
                  ],
                  "initialState": "q0",
                  "acceptingStates": [
                    "q1"
                  ],
                  "transitions": [
                    {
                      "from": "q0",
                      "symbol": "a",
                      "to": "q1"
                    },
                    {
                      "from": "q1",
                      "symbol": "b",
                      "to": "q0"
                    }
                  ]
                }
                """;

        assertEquals(expected, AutomatonSerializer.toJson(dfa));
    }

    @Test
    @DisplayName("Should serialize an NFA with multiple targets per symbol in sorted order")
    void testSerializeNFA() {
        NFA nfa =
                new NFA.Builder()
                        .addState("start", false)
                        .addState("end", true)
                        .setInitialState("start")
                        .addTransition("start", '0', "start")
                        .addTransition("start", '0', "end")
                        .build();

        String expected =
                """
                {
                  "type": "NFA",
                  "alphabet": [
                    "0"
                  ],
                  "states": [
                    "end",
                    "start"
                  ],
                  "initialState": "start",
                  "acceptingStates": [
                    "end"
                  ],
                  "transitions": [
                    {
                      "from": "start",
                      "symbol": "0",
                      "to": "end"
                    },
                    {
                      "from": "start",
                      "symbol": "0",
                      "to": "start"
                    }
                  ]
                }
                """;

        assertEquals(expected, AutomatonSerializer.toJson(nfa));
    }

    @Test
    @DisplayName(
            "Should serialize an ENFA distinguishing null epsilon transitions from literal 'ε'")
    void testSerializeENFAWithEpsilonAndLiteralEpsilonSymbol() {
        ENFA enfa =
                new ENFA.Builder()
                        .addState("A", false)
                        .addState("B", true)
                        .addState("C", false)
                        .setInitialState("A")
                        .addEpsilonTransition("A", "B")
                        .addTransition("A", 'a', "C")
                        .addTransition("A", 'ε', "B")
                        .build();

        String expected =
                """
                {
                  "type": "ENFA",
                  "alphabet": [
                    "a",
                    "ε"
                  ],
                  "states": [
                    "A",
                    "B",
                    "C"
                  ],
                  "initialState": "A",
                  "acceptingStates": [
                    "B"
                  ],
                  "transitions": [
                    {
                      "from": "A",
                      "symbol": "a",
                      "to": "C"
                    },
                    {
                      "from": "A",
                      "symbol": "ε",
                      "to": "B"
                    },
                    {
                      "from": "A",
                      "symbol": null,
                      "to": "B"
                    }
                  ]
                }
                """;

        assertEquals(expected, AutomatonSerializer.toJson(enfa));
    }

    @Test
    @DisplayName("Should handle empty alphabet, no accepting states, and unreachable states")
    void testEmptyAlphabetAndUnreachableStates() {
        DFA dfa =
                new DFA.Builder()
                        .addState("q0", false)
                        .addState("q_unreachable", false)
                        .setInitialState("q0")
                        .build();

        String expected =
                """
                {
                  "type": "DFA",
                  "alphabet": [],
                  "states": [
                    "q0",
                    "q_unreachable"
                  ],
                  "initialState": "q0",
                  "acceptingStates": [],
                  "transitions": []
                }
                """;

        assertEquals(expected, AutomatonSerializer.toJson(dfa));
    }

    @Test
    @DisplayName("Should produce identical JSON regardless of state and transition insertion order")
    void testDeterministicOutputRegardlessOfInsertionOrder() {
        NFA first =
                new NFA.Builder()
                        .addState("q2", true)
                        .addState("q0", false)
                        .addState("q1", true)
                        .setInitialState("q0")
                        .addTransition("q1", 'b', "q2")
                        .addTransition("q0", 'b', "q1")
                        .addTransition("q0", 'a', "q2")
                        .addTransition("q0", 'a', "q1")
                        .build();

        NFA second =
                new NFA.Builder()
                        .addState("q0", false)
                        .addState("q1", true)
                        .addState("q2", true)
                        .setInitialState("q0")
                        .addTransition("q0", 'a', "q1")
                        .addTransition("q0", 'a', "q2")
                        .addTransition("q0", 'b', "q1")
                        .addTransition("q1", 'b', "q2")
                        .build();

        assertEquals(AutomatonSerializer.toJson(first), AutomatonSerializer.toJson(second));
    }

    @Test
    @DisplayName("Should properly escape JSON special characters, control characters, and HTML")
    void testJsonStringEscaping() {
        String complexName = "s\"\\/\b\f\n\r\t\u0001<script>&";
        DFA dfa =
                new DFA.Builder()
                        .addState(complexName, true)
                        .setInitialState(complexName)
                        .addTransition(complexName, '"', complexName)
                        .build();

        String json = AutomatonSerializer.toJson(dfa);

        assertTrue(json.contains("s\\\"\\\\/\\b\\f\\n\\r\\t\\u0001\\u003cscript\\u003e\\u0026"));
        assertTrue(json.contains("\"symbol\": \"\\\"\""));
        assertFalse(json.contains("<script>"));
    }

    @Test
    @DisplayName("Should reject null or unsupported Automaton implementations")
    void testValidation() {
        assertThrows(NullPointerException.class, () -> AutomatonSerializer.toJson(null));

        Automaton customAutomaton = mock(Automaton.class);
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> AutomatonSerializer.toJson(customAutomaton));
        assertTrue(exception.getMessage().contains("Unsupported automaton type"));
    }

    @Test
    @DisplayName("Utility class constructor should throw UnsupportedOperationException")
    void testUtilityClassConstructorThrowsException() throws Exception {
        Constructor<AutomatonSerializer> constructor =
                AutomatonSerializer.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        InvocationTargetException exception =
                assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        assertTrue(exception.getCause().getMessage().contains("Utility class"));
    }

    @Nested
    @DisplayName("Cyclic I/O & Deserialization (fromJson)")
    class DeserializationAndCyclicIoTests {

        @Test
        @DisplayName(
                "Should round-trip a compiled Regex DFA with deep equality and language equivalence")
        void testCyclicIoCompiledRegexDfa() {
            DFA original = new Regex("(a|b)*abb").getAutomaton();

            String json = AutomatonSerializer.toJson(original);
            Automaton deserialized = AutomatonSerializer.fromJson(json);

            DFA restored = assertInstanceOf(DFA.class, deserialized);
            assertNotSame(original, restored);
            assertEquals(original.getStates(), restored.getStates());
            assertEquals(original.getInitialState(), restored.getInitialState());
            assertEquals(original.getFinalStates(), restored.getFinalStates());
            assertEquals(original.getTransitionTable(), restored.getTransitionTable());
            assertEquals(json, AutomatonSerializer.toJson(restored));
            assertTrue(AutomataOperations.areEquivalent(original, restored));

            for (String sample : LanguageGenerator.generateAll(original, 6)) {
                assertTrue(restored.accepts(sample));
            }
            assertFalse(restored.accepts("abba"));
            assertFalse(restored.accepts(""));
        }

        @Test
        @DisplayName("Should round-trip an NFA with deep equality and language equivalence")
        void testCyclicIoNfa() {
            NFA original =
                    new NFA.Builder()
                            .addState("q0", false)
                            .addState("q1", false)
                            .addState("q2", true)
                            .setInitialState("q0")
                            .addTransition("q0", '0', "q0")
                            .addTransition("q0", '1', "q0")
                            .addTransition("q0", '0', "q1")
                            .addTransition("q1", '1', "q2")
                            .build();

            String json = AutomatonSerializer.toJson(original);
            NFA restored = assertInstanceOf(NFA.class, AutomatonSerializer.fromJson(json));

            assertEquals(original.getStates(), restored.getStates());
            assertEquals(original.getInitialState(), restored.getInitialState());
            assertEquals(original.getFinalStates(), restored.getFinalStates());
            assertEquals(original.getTransitionTable(), restored.getTransitionTable());
            assertEquals(json, AutomatonSerializer.toJson(restored));
            assertTrue(AutomataOperations.areEquivalent(original, restored));
        }

        @Test
        @DisplayName("Should round-trip an ENFA with epsilon transitions and literal 'ε' symbols")
        void testCyclicIoEnfa() {
            ENFA original =
                    new ENFA.Builder()
                            .addState("q0", false)
                            .addState("q1", true)
                            .addState("q2", false)
                            .setInitialState("q0")
                            .addTransition("q0", 'a', "q0")
                            .addEpsilonTransition("q0", "q1")
                            .addTransition("q1", 'b', "q1")
                            .addTransition("q1", 'ε', "q2")
                            .build();

            String json = AutomatonSerializer.toJson(original);
            ENFA restored = assertInstanceOf(ENFA.class, AutomatonSerializer.fromJson(json));

            assertEquals(original.getStates(), restored.getStates());
            assertEquals(original.getInitialState(), restored.getInitialState());
            assertEquals(original.getFinalStates(), restored.getFinalStates());
            assertEquals(original.getTransitionTable(), restored.getTransitionTable());
            assertEquals(json, AutomatonSerializer.toJson(restored));
            assertTrue(AutomataOperations.areEquivalent(original, restored));
        }

        @Test
        @DisplayName(
                "Should round-trip state names containing special, unicode, and escaped characters")
        void testCyclicIoSpecialCharactersAndUnicodeEscapes() {
            String trickyState = "q\"\\/\b\f\n\r\t\u0001<tag>&";
            DFA original =
                    new DFA.Builder()
                            .addState(trickyState, true)
                            .setInitialState(trickyState)
                            .addTransition(trickyState, '"', trickyState)
                            .build();

            String json = AutomatonSerializer.toJson(original);
            DFA restored = assertInstanceOf(DFA.class, AutomatonSerializer.fromJson(json));

            assertEquals(original.getStates(), restored.getStates());
            assertEquals(original.getInitialState(), restored.getInitialState());
            assertEquals(original.getTransitionTable(), restored.getTransitionTable());
            assertTrue(restored.accepts("\"\""));
        }

        @Test
        @DisplayName("Should enforce immutability on deserialized automata collections")
        void testDeserializedAutomatonIsImmutable() {
            DFA original = new Regex("a+").getAutomaton();
            DFA restored =
                    assertInstanceOf(
                            DFA.class,
                            AutomatonSerializer.fromJson(AutomatonSerializer.toJson(original)));

            State dummy = new State("x", false);
            var states = restored.getStates();
            var finalStates = restored.getFinalStates();
            var transitionTable = restored.getTransitionTable();

            assertThrows(UnsupportedOperationException.class, () -> states.add(dummy));
            assertThrows(UnsupportedOperationException.class, () -> finalStates.add(dummy));
            assertThrows(UnsupportedOperationException.class, transitionTable::clear);
        }
    }

    @Nested
    @DisplayName("Deserialization Error Handling & Security Validation")
    class DeserializationValidationTests {

        @Test
        @DisplayName("Should reject null, blank, or oversized JSON inputs")
        void testNullBlankAndOversizedInputs() {
            assertThrows(NullPointerException.class, () -> AutomatonSerializer.fromJson(null));
            assertThrows(IllegalArgumentException.class, () -> AutomatonSerializer.fromJson(""));
            assertThrows(
                    IllegalArgumentException.class, () -> AutomatonSerializer.fromJson("   \n\t "));

            String oversized = "{".repeat(5_000_001);
            IllegalArgumentException ex =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> AutomatonSerializer.fromJson(oversized));
            assertTrue(ex.getMessage().contains("exceeds maximum allowed length"));
        }

        @Test
        @DisplayName("Should reject malformed JSON syntax, trailing tokens, and deep nesting")
        void testMalformedJsonSyntax() {
            assertThrows(IllegalArgumentException.class, () -> AutomatonSerializer.fromJson("[]"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> AutomatonSerializer.fromJson("{\"type\": \"DFA\""));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> AutomatonSerializer.fromJson("{\"type\": \"DFA\",}"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> AutomatonSerializer.fromJson("{\"type\": \"DFA\"} trailing"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> AutomatonSerializer.fromJson("{\"type\": \"unterminated}"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> AutomatonSerializer.fromJson("{\"type\": \"bad\\escape\"}"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> AutomatonSerializer.fromJson("{\"type\": \"bad\\u12\"}"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> AutomatonSerializer.fromJson("{\"type\": \"bad\\u12GZ\"}"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> AutomatonSerializer.fromJson("{\"type\": \"line\nbreak\"}"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> AutomatonSerializer.fromJson("{\"type\": @}"));

            String deeplyNested = "{\"a\": " + "[".repeat(40) + "]".repeat(40) + "}";
            IllegalArgumentException depthEx =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> AutomatonSerializer.fromJson(deeplyNested));
            assertTrue(depthEx.getMessage().contains("nesting depth"));
        }

        @Test
        @DisplayName("Should reject missing, duplicate, unknown, or mistyped JSON fields")
        void testSchemaFieldValidation() {
            // Missing required field
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "alphabet": [],
                                      "states": ["q0"],
                                      "acceptingStates": [],
                                      "transitions": []
                                    }
                                    """));

            // Unknown root field
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "alphabet": [],
                                      "states": ["q0"],
                                      "initialState": "q0",
                                      "acceptingStates": [],
                                      "transitions": [],
                                      "extra": true
                                    }
                                    """));

            // Duplicate JSON key
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "type": "NFA",
                                      "alphabet": [],
                                      "states": ["q0"],
                                      "initialState": "q0",
                                      "acceptingStates": [],
                                      "transitions": []
                                    }
                                    """));

            // Wrong field types (numbers/booleans/objects instead of expected strings/arrays)
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": 123,
                                      "alphabet": [],
                                      "states": ["q0"],
                                      "initialState": "q0",
                                      "acceptingStates": [],
                                      "transitions": []
                                    }
                                    """));

            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "alphabet": false,
                                      "states": ["q0"],
                                      "initialState": "q0",
                                      "acceptingStates": [],
                                      "transitions": []
                                    }
                                    """));
        }

        @Test
        @DisplayName("Should reject invalid automaton semantics and Builder constraint violations")
        void testSemanticValidation() {
            // Unsupported automaton type
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "PDA",
                                      "alphabet": [],
                                      "states": ["q0"],
                                      "initialState": "q0",
                                      "acceptingStates": [],
                                      "transitions": []
                                    }
                                    """));

            // Empty states array
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "alphabet": [],
                                      "states": [],
                                      "initialState": "q0",
                                      "acceptingStates": [],
                                      "transitions": []
                                    }
                                    """));

            // Undeclared initialState
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "alphabet": [],
                                      "states": ["q0"],
                                      "initialState": "q_missing",
                                      "acceptingStates": [],
                                      "transitions": []
                                    }
                                    """));

            // Undeclared accepting state
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "alphabet": [],
                                      "states": ["q0"],
                                      "initialState": "q0",
                                      "acceptingStates": ["q_missing"],
                                      "transitions": []
                                    }
                                    """));

            // Multi-character alphabet symbol
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "alphabet": ["ab"],
                                      "states": ["q0"],
                                      "initialState": "q0",
                                      "acceptingStates": [],
                                      "transitions": []
                                    }
                                    """));

            // Epsilon transition in non-ENFA automaton
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "alphabet": [],
                                      "states": ["q0", "q1"],
                                      "initialState": "q0",
                                      "acceptingStates": ["q1"],
                                      "transitions": [
                                        {"from": "q0", "symbol": null, "to": "q1"}
                                      ]
                                    }
                                    """));

            // Transition referencing undeclared target state (caught by Builder)
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "alphabet": ["a"],
                                      "states": ["q0"],
                                      "initialState": "q0",
                                      "acceptingStates": [],
                                      "transitions": [
                                        {"from": "q0", "symbol": "a", "to": "q_unknown"}
                                      ]
                                    }
                                    """));

            // Transition symbol not in declared alphabet
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "alphabet": ["a"],
                                      "states": ["q0"],
                                      "initialState": "q0",
                                      "acceptingStates": [],
                                      "transitions": [
                                        {"from": "q0", "symbol": "b", "to": "q0"}
                                      ]
                                    }
                                    """));

            // Non-deterministic duplicate (from, symbol) transition in DFA
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            AutomatonSerializer.fromJson(
                                    """
                                    {
                                      "type": "DFA",
                                      "alphabet": ["a"],
                                      "states": ["q0", "q1"],
                                      "initialState": "q0",
                                      "acceptingStates": ["q1"],
                                      "transitions": [
                                        {"from": "q0", "symbol": "a", "to": "q0"},
                                        {"from": "q0", "symbol": "a", "to": "q1"}
                                      ]
                                    }
                                    """));
        }
    }
}
