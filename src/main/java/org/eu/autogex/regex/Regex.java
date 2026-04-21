package org.eu.autogex.regex;

import org.eu.autogex.algorithms.Converter;
import org.eu.autogex.algorithms.Minimizer;
import org.eu.autogex.algorithms.ThompsonConstructor;
import org.eu.autogex.export.GraphvizExporter;
import org.eu.autogex.export.MermaidExporter;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.regex.ast.RegexNode;
import org.eu.autogex.regex.parser.RegexParser;

/**
 * Facade class representing a compiled Regular Expression. Under the hood, it parses the regex,
 * builds an ENFA, converts it to a DFA, and minimizes it.
 */
public class Regex {

    private final String pattern;
    private final DFA minimalDfa;

    /**
     * Compiles the given regular expression into a highly optimized Minimal DFA.
     *
     * @param pattern The regular expression string (e.g., "(a|b)*abb").
     * @throws IllegalArgumentException If the regex syntax is invalid.
     */
    public Regex(String pattern) {
        this.pattern = pattern;

        // 1. Parse string to AST
        RegexNode ast = RegexParser.parse(pattern);

        // 2. Apply Thompson's Construction (AST -> ENFA)
        ENFA enfa = ThompsonConstructor.construct(ast);

        // 3. Apply Subset Construction (ENFA -> DFA)
        DFA rawDfa = Converter.enfaToDfa(enfa);

        // 4. Apply Moore's Partitioning (DFA -> Minimal DFA)
        this.minimalDfa = Minimizer.minimize(rawDfa);
    }

    /**
     * Checks if the input string perfectly matches the regular expression.
     *
     * @param input The string to test.
     * @return True if the string is accepted by the underlying automaton, false otherwise.
     */
    public boolean matches(String input) {
        return minimalDfa.accepts(input);
    }

    /**
     * Exports the compiled minimal DFA to the Graphviz DOT language format. This string can be
     * rendered using Graphviz tools or online viewers.
     *
     * @return The DOT graph representation.
     */
    public String toDotGraph() {
        return GraphvizExporter.toDot(minimalDfa);
    }

    /**
     * Exports the compiled minimal DFA to the Mermaid.js stateDiagram-v2 format. This string can be
     * natively rendered in GitHub Markdown without external viewers.
     *
     * @return The Mermaid.js graph representation.
     */
    public String toMermaidGraph() {
        return MermaidExporter.toMermaid(minimalDfa);
    }

    /**
     * Retrieves the underlying compiled and minimized DFA.
     *
     * @return The minimal DFA.
     */
    public DFA getAutomaton() {
        return minimalDfa;
    }

    /**
     * Retrieves the original regular expression pattern.
     *
     * @return The pattern string.
     */
    public String getPattern() {
        return pattern;
    }
}
