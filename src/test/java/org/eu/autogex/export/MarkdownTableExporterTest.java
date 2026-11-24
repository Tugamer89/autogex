package org.eu.autogex.export;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;
import org.junit.jupiter.api.Test;

class MarkdownTableExporterTest {

    @Test
    void testExportDFAMarksInitialAndAcceptingStates() {
        DFA dfa =
                new DFA.Builder()
                        .addState("q0", false)
                        .addState("q1", true)
                        .setInitialState("q0")
                        .addTransition("q0", 'a', "q1")
                        .addTransition("q1", 'b', "q0")
                        .build();

        String expected =
                "| State | a | b |\n"
                        + "| --- | --- | --- |\n"
                        + "| -> q0 | q1 | - |\n"
                        + "| *q1 | - | q0 |\n";

        assertEquals(expected, MarkdownTableExporter.toMarkdownTable(dfa));
    }

    @Test
    void testInitialAcceptingStateGetsBothMarkers() {
        DFA dfa =
                new DFA.Builder()
                        .addState("s", true)
                        .setInitialState("s")
                        .addTransition("s", 'x', "s")
                        .build();

        String table = MarkdownTableExporter.toMarkdownTable(dfa);

        assertTrue(table.contains("| -> *s | s |"), "Initial accepting state needs both markers");
    }

    @Test
    void testExportDFAWithoutTransitionsHasOnlyStateColumn() {
        DFA dfa =
                new DFA.Builder()
                        .addState("q0", true)
                        .addState("q1", false)
                        .setInitialState("q0")
                        .build();

        String expected = "| State |\n| --- |\n| -> *q0 |\n| q1 |\n";

        assertEquals(expected, MarkdownTableExporter.toMarkdownTable(dfa));
    }

    @Test
    void testExportNFAFormatsSortedSetsAndEmptyCells() {
        NFA nfa =
                new NFA.Builder()
                        .addState("start", false)
                        .addState("end", true)
                        .setInitialState("start")
                        .addTransition("start", '0', "start")
                        .addTransition("start", '0', "end")
                        .build();

        String expected =
                "| State | 0 |\n"
                        + "| --- | --- |\n"
                        + "| *end | ∅ |\n"
                        + "| -> start | {end, start} |\n";

        assertEquals(expected, MarkdownTableExporter.toMarkdownTable(nfa));
    }

    @Test
    void testExportENFAHasEpsilonColumn() {
        ENFA enfa =
                new ENFA.Builder()
                        .addState("A", false)
                        .addState("B", true)
                        .addState("C", false)
                        .setInitialState("A")
                        .addEpsilonTransition("A", "B")
                        .addEpsilonTransition("A", "C")
                        .addTransition("B", 'a', "C")
                        .build();

        String expected =
                "| State | a | ε |\n"
                        + "| --- | --- | --- |\n"
                        + "| -> A | ∅ | {B, C} |\n"
                        + "| *B | {C} | ∅ |\n"
                        + "| C | ∅ | ∅ |\n";

        assertEquals(expected, MarkdownTableExporter.toMarkdownTable(enfa));
    }

    @Test
    void testENFAWithoutEpsilonTransitionsStillHasEpsilonColumn() {
        ENFA enfa =
                new ENFA.Builder()
                        .addState("A", true)
                        .setInitialState("A")
                        .addTransition("A", 'a', "A")
                        .build();

        String table = MarkdownTableExporter.toMarkdownTable(enfa);

        assertTrue(table.startsWith("| State | a | ε |\n"));
    }

    @Test
    void testOutputIsDeterministicRegardlessOfInsertionOrder() {
        DFA first =
                new DFA.Builder()
                        .addState("b", false)
                        .addState("a", true)
                        .setInitialState("b")
                        .addTransition("b", 'y', "a")
                        .addTransition("b", 'x', "b")
                        .build();
        DFA second =
                new DFA.Builder()
                        .addState("a", true)
                        .addState("b", false)
                        .setInitialState("b")
                        .addTransition("b", 'x', "b")
                        .addTransition("b", 'y', "a")
                        .build();

        assertEquals(
                MarkdownTableExporter.toMarkdownTable(first),
                MarkdownTableExporter.toMarkdownTable(second));
    }

    @Test
    void testPipeInStateNameIsEscaped() {
        DFA dfa =
                new DFA.Builder()
                        .addState("a|b", true)
                        .setInitialState("a|b")
                        .addTransition("a|b", 'x', "a|b")
                        .build();

        String table = MarkdownTableExporter.toMarkdownTable(dfa);

        assertTrue(table.contains("a\\|b"), "Pipes must be escaped so the table stays valid");
        assertFalse(table.contains("| -> *a|b |"), "Unescaped pipe would split the cell");
    }

    @Test
    void testUtilityClassConstructorThrowsException() throws Exception {
        Constructor<MarkdownTableExporter> constructor =
                MarkdownTableExporter.class.getDeclaredConstructor();
        assertTrue(
                java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()),
                "Constructor should be private");

        constructor.setAccessible(true);
        InvocationTargetException exception =
                assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }
}
