package org.eu.autogex.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.eu.autogex.core.Automaton;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;
import org.junit.jupiter.api.DisplayName;
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
}
