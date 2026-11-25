package org.eu.autogex;

import java.util.Objects;
import java.util.Set;
import org.eu.autogex.algorithms.AutomataOperations;
import org.eu.autogex.algorithms.Converter;
import org.eu.autogex.algorithms.Minimizer;
import org.eu.autogex.core.Automaton;
import org.eu.autogex.export.GraphvizExporter;
import org.eu.autogex.export.MarkdownTableExporter;
import org.eu.autogex.export.MermaidExporter;
import org.eu.autogex.models.DFA;
import org.eu.autogex.models.ENFA;
import org.eu.autogex.models.NFA;
import org.eu.autogex.regex.Regex;

/**
 * Main facade and metadata provider for the AutoGex library.
 *
 * <p>This utility class provides high-level convenience methods for common workflows—such as
 * compiling regular expressions into minimal DFAs, converting and minimizing automata, computing
 * formal language set operations, and exporting state diagrams—as well as runtime version metadata.
 */
public final class AutoGex {

    /**
     * The current version of the AutoGex library.
     *
     * <p>This value is dynamically retrieved from the JAR manifest (Implementation-Version). If the
     * library is running un-packaged (e.g., during tests), it falls back to a default snapshot
     * value.
     */
    public static final String VERSION =
            resolveVersion(
                    AutoGex.class.getPackage() != null
                            ? AutoGex.class.getPackage().getImplementationVersion()
                            : null);

    /** Private constructor to prevent instantiation of this utility class. */
    private AutoGex() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }

    /**
     * Checks whether two finite automata recognize the exact same regular language.
     *
     * <p>Delegates to {@link AutomataOperations#areEquivalent(Automaton, Automaton)}.
     *
     * <p><b>Example:</b>
     *
     * <pre>{@code
     * boolean equivalent = AutoGex.areEquivalent(firstAutomaton, secondAutomaton);
     * }</pre>
     *
     * @param a1 the first finite automaton, must not be {@code null}
     * @param a2 the second finite automaton, must not be {@code null}
     * @return {@code true} if both automata recognize the same language, {@code false} otherwise
     * @throws NullPointerException if {@code a1} or {@code a2} is {@code null}
     */
    public static boolean areEquivalent(Automaton a1, Automaton a2) {
        Objects.requireNonNull(a1, "a1 must not be null");
        Objects.requireNonNull(a2, "a2 must not be null");
        return AutomataOperations.areEquivalent(a1, a2);
    }

    /**
     * Compiles a regular expression pattern into an equivalent minimal {@link DFA}.
     *
     * @param pattern the regular expression pattern to compile
     * @return a minimal {@link DFA} recognizing the language described by {@code pattern}
     */
    public static DFA compile(String pattern) {
        return new Regex(pattern).getAutomaton();
    }

    /**
     * Checks whether the given input string matches the specified regular expression pattern.
     *
     * @param pattern the regular expression pattern
     * @param input the input string to evaluate
     * @return {@code true} if {@code input} belongs to the language of {@code pattern}, {@code
     *     false} otherwise
     */
    public static boolean matches(String pattern, String input) {
        return compile(pattern).accepts(input);
    }

    /**
     * Minimizes a {@link DFA} using Moore's partition refinement algorithm.
     *
     * @param dfa the deterministic finite automaton to minimize
     * @return an equivalent minimal {@link DFA}
     */
    public static DFA minimize(DFA dfa) {
        return Minimizer.minimize(dfa);
    }

    /**
     * Converts an {@link ENFA} (with ε-transitions) into an equivalent minimal {@link DFA}.
     *
     * @param enfa the epsilon-NFA to convert and minimize
     * @return an equivalent minimal {@link DFA}
     */
    public static DFA toMinimalDfa(ENFA enfa) {
        return Minimizer.minimize(Converter.enfaToDfa(enfa));
    }

    /**
     * Computes the union ({@code L1 ∪ L2}) of two deterministic finite automata.
     *
     * @param first the first DFA
     * @param second the second DFA
     * @return a {@link DFA} recognizing the union of both languages
     */
    public static DFA union(DFA first, DFA second) {
        return AutomataOperations.union(first, second);
    }

    /**
     * Computes the intersection ({@code L1 ∩ L2}) of two deterministic finite automata.
     *
     * @param first the first DFA
     * @param second the second DFA
     * @return a {@link DFA} recognizing the intersection of both languages
     */
    public static DFA intersection(DFA first, DFA second) {
        return AutomataOperations.intersection(first, second);
    }

    /**
     * Computes the set difference ({@code L1 \ L2}) of two deterministic finite automata.
     *
     * @param first the base DFA
     * @param second the DFA whose language is subtracted
     * @return a {@link DFA} recognizing strings accepted by {@code first} but not by {@code second}
     */
    public static DFA difference(DFA first, DFA second) {
        return AutomataOperations.difference(first, second);
    }

    /**
     * Computes the complement ({@code Σ* \ L}) of a {@link DFA} with respect to the given alphabet.
     *
     * @param dfa the DFA to complement
     * @param alphabet the reference alphabet over which to compute the complement
     * @return a {@link DFA} recognizing the complement language
     */
    public static DFA complement(DFA dfa, Set<Character> alphabet) {
        return AutomataOperations.complement(dfa, alphabet);
    }

    /**
     * Exports a {@link DFA} to a Mermaid.js state diagram ({@code stateDiagram-v2}).
     *
     * @param dfa the deterministic finite automaton to render
     * @return the Mermaid diagram definition string
     */
    public static String toMermaid(DFA dfa) {
        return MermaidExporter.toMermaid(dfa);
    }

    /**
     * Exports an {@link NFA} to a Mermaid.js state diagram ({@code stateDiagram-v2}).
     *
     * @param nfa the non-deterministic finite automaton to render
     * @return the Mermaid diagram definition string
     */
    public static String toMermaid(NFA nfa) {
        return MermaidExporter.toMermaid(nfa);
    }

    /**
     * Exports an {@link ENFA} to a Mermaid.js state diagram ({@code stateDiagram-v2}).
     *
     * @param enfa the epsilon-NFA to render
     * @return the Mermaid diagram definition string
     */
    public static String toMermaid(ENFA enfa) {
        return MermaidExporter.toMermaid(enfa);
    }

    /**
     * Exports a {@link DFA} to the Graphviz DOT language.
     *
     * @param dfa the deterministic finite automaton to render
     * @return the Graphviz DOT graph definition string
     */
    public static String toGraphviz(DFA dfa) {
        return GraphvizExporter.toDot(dfa);
    }

    /**
     * Exports an {@link NFA} to the Graphviz DOT language.
     *
     * @param nfa the non-deterministic finite automaton to render
     * @return the Graphviz DOT graph definition string
     */
    public static String toGraphviz(NFA nfa) {
        return GraphvizExporter.toDot(nfa);
    }

    /**
     * Exports an {@link ENFA} to the Graphviz DOT language.
     *
     * @param enfa the epsilon-NFA to render
     * @return the Graphviz DOT graph definition string
     */
    public static String toGraphviz(ENFA enfa) {
        return GraphvizExporter.toDot(enfa);
    }

    /**
     * Exports a {@link DFA} as a GitHub-flavored Markdown state-transition table.
     *
     * @param dfa the deterministic finite automaton to render
     * @return the Markdown transition table string
     */
    public static String toMarkdownTable(DFA dfa) {
        return MarkdownTableExporter.toMarkdownTable(dfa);
    }

    /**
     * Exports an {@link NFA} as a GitHub-flavored Markdown state-transition table.
     *
     * @param nfa the non-deterministic finite automaton to render
     * @return the Markdown transition table string
     */
    public static String toMarkdownTable(NFA nfa) {
        return MarkdownTableExporter.toMarkdownTable(nfa);
    }

    /**
     * Exports an {@link ENFA} as a GitHub-flavored Markdown state-transition table.
     *
     * @param enfa the epsilon-NFA to render
     * @return the Markdown transition table string
     */
    public static String toMarkdownTable(ENFA enfa) {
        return MarkdownTableExporter.toMarkdownTable(enfa);
    }

    /**
     * Dynamically resolves the library version from the package metadata.
     *
     * @param implementationVersion The version string retrieved from the package manifest, or null.
     * @return The implementation version or a fallback default string.
     */
    static String resolveVersion(String implementationVersion) {
        return implementationVersion != null ? implementationVersion : "1.0.0-SNAPSHOT";
    }
}
