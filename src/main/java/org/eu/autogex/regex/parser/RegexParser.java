package org.eu.autogex.regex.parser;

import org.eu.autogex.regex.ast.RegexNode;
import org.eu.autogex.regex.ast.RegexNode.*;

/**
 * A Recursive Descent Parser that converts a Regular Expression string into an Abstract Syntax Tree (AST).
 * * Grammar implemented:
 * Regex  -> Term ( '|' Term )*
 * Term   -> Factor ( Factor )* (Implicit concatenation)
 * Factor -> Base ( '*' )*
 * Base   -> char | '(' Regex ')'
 */
public class RegexParser {

    private final String input;
    private int position;

    /**
     * Private constructor to enforce the use of the static parse method.
     */
    private RegexParser(String input) {
        this.input = input.replaceAll("\\s+", ""); // Remove whitespaces for simplicity
        this.position = 0;
    }

    /**
     * Parses a regular expression string into an AST.
     *
     * @param regex The regular expression string to parse.
     * @return The root node of the generated AST.
     * @throws IllegalArgumentException If the regex syntax is invalid.
     */
    public static RegexNode parse(String regex) {
        if (regex == null || regex.isEmpty()) {
            throw new IllegalArgumentException("Regular expression cannot be null or empty.");
        }
        
        RegexParser parser = new RegexParser(regex);
        RegexNode root = parser.parseRegex();

        // If we finished parsing but haven't reached the end of the string, there's a syntax error
        if (parser.hasNext()) {
            throw new IllegalArgumentException("Unexpected character at index " + parser.position + ": " + parser.peek());
        }

        return root;
    }

    // --- Recursive Parsing Methods ---

    /**
     * Parses a full Regex, handling the Union ('|') operator.
     */
    private RegexNode parseRegex() {
        RegexNode left = parseTerm();

        while (match('|')) {
            RegexNode right = parseTerm();
            left = new UnionNode(left, right);
        }

        return left;
    }

    /**
     * Parses a Term, handling implicit Concatenation.
     */
    private RegexNode parseTerm() {
        RegexNode left = parseFactor();

        // Continue concatenating as long as the next character is part of a new Factor
        // (i.e., not a closing parenthesis, not a union operator, and not EOF)
        while (hasNext() && peek() != '|' && peek() != ')') {
            RegexNode right = parseFactor();
            left = new ConcatNode(left, right);
        }

        return left;
    }

    /**
     * Parses a Factor, handling the Kleene Star ('*') operator.
     */
    private RegexNode parseFactor() {
        RegexNode base = parseBase();

        while (match('*')) {
            base = new StarNode(base);
        }

        return base;
    }

    /**
     * Parses a Base, which is either a parenthesized Regex or a single Literal.
     */
    private RegexNode parseBase() {
        if (match('(')) {
            RegexNode node = parseRegex();
            if (!match(')')) {
                throw new IllegalArgumentException("Missing closing parenthesis ')'.");
            }
            return node;
        }

        if (!hasNext() || peek() == '|' || peek() == ')' || peek() == '*') {
            throw new IllegalArgumentException("Invalid token at index " + position + ". Expected literal or '('.");
        }

        return new LiteralNode(next());
    }

    // --- Utility Methods for Token Navigation ---

    private boolean hasNext() {
        return position < input.length();
    }

    private char peek() {
        return input.charAt(position);
    }

    private char next() {
        return input.charAt(position++);
    }

    private boolean match(char expected) {
        if (hasNext() && peek() == expected) {
            position++;
            return true;
        }
        return false;
    }
}