package org.eu.autogex.regex;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RegexTest {

    @Test
    void testRegexLiteral() {
        Regex regex = new Regex("abc");

        assertEquals("abc", regex.getPattern());
        assertTrue(regex.matches("abc"), "Should match the exact literal string");
        assertFalse(regex.matches("ab"), "Should reject partial matches");
        assertFalse(regex.matches("abcd"), "Should reject strings with extra characters");
    }

    @Test
    void testRegexUnion() {
        Regex regex = new Regex("a|b");

        assertTrue(regex.matches("a"), "Should match left side of union");
        assertTrue(regex.matches("b"), "Should match right side of union");
        assertFalse(regex.matches("ab"), "Should reject concatenation when union is expected");
        assertFalse(regex.matches("c"), "Should reject unrelated characters");
    }

    @Test
    void testRegexKleeneStar() {
        Regex regex = new Regex("a*");

        assertTrue(regex.matches(""), "Star operator must accept empty string");
        assertTrue(regex.matches("a"), "Star operator must accept single occurrence");
        assertTrue(regex.matches("aaaaa"), "Star operator must accept multiple occurrences");
        assertFalse(regex.matches("b"), "Should reject unrelated characters");
        assertFalse(
                regex.matches("aaab"), "Should reject strings ending with unrelated characters");
    }

    @Test
    void testRegexPlusOperator() {
        Regex regex = new Regex("a+");

        assertFalse(regex.matches(""), "Plus operator must reject empty string");
        assertTrue(regex.matches("a"), "Plus operator must accept single occurrence");
        assertTrue(regex.matches("aaaaa"), "Plus operator must accept multiple occurrences");
        assertFalse(regex.matches("b"), "Should reject unrelated characters");
    }

    @Test
    void testRegexOptionalOperator() {
        Regex regex = new Regex("a?");

        assertTrue(regex.matches(""), "Optional operator must accept empty string");
        assertTrue(regex.matches("a"), "Optional operator must accept single occurrence");
        assertFalse(regex.matches("aa"), "Optional operator must reject multiple occurrences");
    }

    @Test
    void testRegexWildcardOperator() {
        Regex regex = new Regex("a.c");

        assertTrue(regex.matches("abc"), "Wildcard should match standard letter");
        assertTrue(regex.matches("aXc"), "Wildcard should match capital letter");
        assertTrue(regex.matches("a9c"), "Wildcard should match digit");
        assertTrue(regex.matches("a-c"), "Wildcard should match symbol");
        assertTrue(regex.matches("a c"), "Wildcard should match space (in input)");

        assertFalse(regex.matches("ac"), "Wildcard must match exactly one character");
        assertFalse(regex.matches("abbc"), "Wildcard cannot match multiple characters");
    }

    @Test
    void testRegexCharacterClasses() {
        Regex regex = new Regex("[a-cx-z\\d]+");

        assertTrue(regex.matches("a"), "Matches single character from class");
        assertTrue(regex.matches("acxy092"), "Matches multiple class characters mixed");
        assertFalse(regex.matches("d"), "Rejects characters outside the class range");
        assertFalse(regex.matches("acM2"), "Rejects strings containing invalid characters");
    }

    @Test
    void testComplexRegexPipeline() {
        Regex regex = new Regex("(a|b)*abb");

        // Accepted strings
        assertTrue(regex.matches("abb"));
        assertTrue(regex.matches("aabb"));
        assertTrue(regex.matches("babb"));
        assertTrue(regex.matches("abaabbabaabb"));

        // Rejected strings
        assertFalse(regex.matches("ab"), "Too short");
        assertFalse(regex.matches("abba"), "Does not end with abb");
        assertFalse(regex.matches(""), "Empty string cannot end with abb");
    }

    @Test
    void testRegexOptimizationExcellence() {
        Regex regex = new Regex("(a|b)*abb");
        // The minimal DFA for "(a|b)*abb" requires EXACTLY 4 states.
        assertEquals(
                4,
                regex.getAutomaton().getStates().size(),
                "The minimized DFA for (a|b)*abb must have exactly 4 states");
    }

    @Test
    void testToDotGraph() {
        Regex regex = new Regex("(a|b)*abb");
        String dotGraph = regex.toDotGraph();

        assertNotNull(dotGraph, "DOT graph should not be null");
        assertTrue(dotGraph.startsWith("digraph Automaton {"), "Must be a valid DOT digraph");
        assertTrue(dotGraph.contains("rankdir=LR;"), "Must be left-to-right oriented");
        assertTrue(
                dotGraph.contains("shape = doublecircle"), "Must contain at least one final state");
        assertTrue(dotGraph.contains("label=\"a\""), "Must contain transitions for 'a'");
        assertTrue(dotGraph.contains("label=\"b\""), "Must contain transitions for 'b'");
    }

    @Test
    void testToMermaidGraph() {
        Regex regex = new Regex("(a|b)*abb");
        String mermaidGraph = regex.toMermaidGraph();

        assertNotNull(mermaidGraph, "Mermaid graph should not be null");
        assertTrue(
                mermaidGraph.startsWith("stateDiagram-v2"),
                "Must start with stateDiagram-v2 declaration");
        assertTrue(mermaidGraph.contains("direction LR"), "Must be left-to-right oriented");
        assertTrue(mermaidGraph.contains("[*] -->"), "Must contain an initial state entry point");
        assertTrue(
                mermaidGraph.contains("--> [*]"),
                "Must contain at least one exit point for final states");
        assertTrue(mermaidGraph.contains(" : a"), "Must contain transitions labeled 'a'");
        assertTrue(mermaidGraph.contains(" : b"), "Must contain transitions labeled 'b'");
    }
}
