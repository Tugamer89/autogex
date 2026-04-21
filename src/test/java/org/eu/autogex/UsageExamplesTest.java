package org.eu.autogex;

import static org.junit.jupiter.api.Assertions.*;

import org.eu.autogex.algorithms.Converter;
import org.eu.autogex.algorithms.Minimizer;
import org.eu.autogex.export.GraphvizExporter;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.regex.Regex;
import org.junit.jupiter.api.Test;

/**
 * Executable documentation. These tests demonstrate the primary use cases of the Autogex library.
 * Users can look at this file to understand how to interact with the API.
 */
class UsageExamplesTest {

    @Test
    void example1_FastRegexCompilation() {
        // SCENARIO: The user wants to check if a string matches a pattern.

        // 1. Compile the string. Under the hood, Autogex builds an AST,
        // applies Thompson's construction, converts to DFA, and minimizes it.
        Regex emailPrefixRegex = new Regex("(a|b|c)*");

        // 2. Test inputs at O(N) speed.
        boolean isValid = emailPrefixRegex.matches("abcbac");

        assertTrue(isValid, "The string should be accepted by the compiled regex");
    }

    @Test
    void example2_VisualizeAutomatonWithGraphviz() {
        // SCENARIO: The user is studying formal languages and wants to visualize a DFA.

        Regex regex = new Regex("(0|1)*00"); // Language: strings ending in 00

        // Export the minimal DFA to DOT language
        String dotGraph = regex.toDotGraph();

        // The user can now print this string and paste it into http://www.webgraphviz.com/
        System.out.println("--- GRAPHVIZ DOT EXPORT ---");
        System.out.println(dotGraph);
        System.out.println("---------------------------");

        assertTrue(dotGraph.contains("digraph"), "The output must be a valid DOT digraph");
    }

    @Test
    void example3_ManualAutomatonConstruction() {
        // SCENARIO: The user wants to manually construct an Epsilon-NFA and optimize it.

        // 1. Build an ENFA fluently
        ENFA myEnfa =
                new ENFA.Builder()
                        .addState("start", false)
                        .addState("end", true)
                        .setInitialState("start")
                        .addTransition("start", 'x', "start")
                        .addEpsilonTransition("start", "end") // Silent jump
                        .build();

        // 2. Convert to DFA (Subset Construction)
        DFA myDfa = Converter.enfaToDfa(myEnfa);

        // 3. Minimize it (Equivalence Classes)
        DFA minimalDfa = Minimizer.minimize(myDfa);

        // 4. Export the ENFA to DOT
        String originalGraph = GraphvizExporter.toDot(myEnfa);

        assertTrue(minimalDfa.accepts("xxxxx"), "The minimized DFA should accept the string");
        assertTrue(
                originalGraph.contains("label=\"ε\""),
                "The exported ENFA should contain epsilon transitions");
    }
}
