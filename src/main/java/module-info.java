/**
 * The AutoGex module provides a zero-dependency engine for Regular Expression compilation and
 * Finite State Automata theory operations.
 *
 * <p>It includes immutable models ({@code DFA}, {@code NFA}, {@code ENFA}), formal algorithms
 * (Thompson's construction, Rabin-Scott subset construction, Moore's minimization, set operations,
 * and language generation), execution tracing, and diagram exporters for Graphviz DOT and
 * Mermaid.js.
 */
module org.eu.autogex {
    // Top-level facade and library metadata
    exports org.eu.autogex;

    // Core automaton contracts, states, transitions, and base builders
    exports org.eu.autogex.core;

    // Concrete immutable finite automata models (DFA, NFA, ENFA)
    exports org.eu.autogex.models;

    // Conversion, minimization, Thompson construction, set operations, and language generation
    exports org.eu.autogex.algorithms;

    // Regular expression facade and AST node hierarchy (parser remains encapsulated)
    exports org.eu.autogex.regex;
    exports org.eu.autogex.regex.ast;

    // Diagram exporters (Graphviz DOT and Mermaid.js)
    exports org.eu.autogex.export;

    // Step-by-step execution tracing diagnostics
    exports org.eu.autogex.trace;
}
