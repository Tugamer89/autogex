/**
 * The AutoGex module provides a comprehensive engine for Regular Expression parsing and Automata
 * theory operations.
 *
 * <p>This module includes tools for converting regex patterns into NFA/DFA, optimizing state
 * machines, and exporting them to various visualization formats.
 */
module org.eu.autogex {
    // --- Public API Exports ---

    /** Exports the root package containing the main Regex entry point. */
    exports org.eu.autogex;

    /** Exports logic for Thompson's construction and DFA minimization. */
    exports org.eu.autogex.algorithms;

    /** Exports core interfaces and base classes for state machines. */
    exports org.eu.autogex.core;

    /** Exports visualization tools for DOT (Graphviz) and Mermaid formats. */
    exports org.eu.autogex.export;

    /** Exports implementation models for NFA, ENFA, and DFA. */
    exports org.eu.autogex.models;

    /** Exports the high-level Regex wrapper. */
    exports org.eu.autogex.regex;

    /** Exports the Abstract Syntax Tree (AST) nodes used by the parser. */
    exports org.eu.autogex.regex.ast;

    /** Exports the recursive descent parser for regular expressions. */
    exports org.eu.autogex.regex.parser;

    /** Exports classes used to trace the execution of an automaton. */
    exports org.eu.autogex.trace;

    // --- Reflection and Testing Permissions ---

    /**
     * Opens packages to allow deep reflection. This is required for JUnit 5 and other testing
     * frameworks to access test classes and private members within the module.
     */
    opens org.eu.autogex;
    opens org.eu.autogex.algorithms;
    opens org.eu.autogex.core;
    opens org.eu.autogex.export;
    opens org.eu.autogex.models;
    opens org.eu.autogex.regex;
    opens org.eu.autogex.regex.ast;
    opens org.eu.autogex.regex.parser;
    opens org.eu.autogex.trace;
}
