package org.eu.autogex.regex.parser;

import java.util.HashSet;
import java.util.Set;
import org.eu.autogex.regex.ast.RegexNode;
import org.eu.autogex.regex.ast.RegexNode.*;

/**
 * A Recursive Descent Parser that converts a Regular Expression string into an Abstract Syntax Tree
 * (AST). * Grammar implemented: Regex -> Term ( '|' Term )* Term -> Factor ( Factor )* (Implicit
 * concatenation) Factor -> Base ( '*' | '+' | '?' )* Base -> char | '(' Regex ')' | '.' | '['
 * charClass ']' | '\d'
 */
public class RegexParser {

    private final String input;
    private int position;

    /** Private constructor to enforce the use of the static parse method. */
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
            throw new IllegalArgumentException(
                    "Unexpected character at index " + parser.position + ": " + parser.peek());
        }

        return root;
    }

    // --- Recursive Parsing Methods ---

    /**
     * Parses a full Regex, handling the Union ('|') operator.
     *
     * @return The RegexNode representing the parsed expression.
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
     *
     * @return The RegexNode representing the term.
     */
    private RegexNode parseTerm() {
        RegexNode left = parseFactor();

        // Continue concatenating as long as the next character is part of a new Factor
        while (hasNext()
                && peek() != '|'
                && peek() != ')'
                && peek() != '*'
                && peek() != '+'
                && peek() != '?') {
            RegexNode right = parseFactor();
            left = new ConcatNode(left, right);
        }

        return left;
    }

    /**
     * Parses a Factor, handling the Kleene Star ('*'), Plus ('+'), and Optional ('?') operators.
     *
     * @return The RegexNode representing the factor.
     */
    private RegexNode parseFactor() {
        RegexNode base = parseBase();

        while (hasNext() && (peek() == '*' || peek() == '+' || peek() == '?')) {
            char op = next();
            if (op == '*') {
                base = new StarNode(base);
            } else if (op == '+') {
                base = new PlusNode(base);
            } else if (op == '?') {
                base = new OptionalNode(base);
            }
        }

        return base;
    }

    /**
     * Parses a Base, which is either a parenthesized Regex, a Character Class, a Wildcard, or a
     * single Literal.
     *
     * @return The RegexNode representing the base element.
     * @throws IllegalArgumentException if the syntax is incorrect.
     */
    private RegexNode parseBase() {
        if (match('(')) {
            RegexNode node = parseRegex();
            if (!match(')')) {
                throw new IllegalArgumentException("Missing closing parenthesis ')'.");
            }
            return node;
        }

        if (match('.')) {
            return new WildcardNode();
        }

        if (match('[')) {
            return parseCharClass();
        }

        if (match('\\')) {
            char escaped = next();
            if (escaped == 'd') {
                return new CharClassNode(getDigitSet());
            }
            return new LiteralNode(escaped);
        }

        if (!hasNext()
                || peek() == '|'
                || peek() == ')'
                || peek() == '*'
                || peek() == '+'
                || peek() == '?') {
            throw new IllegalArgumentException(
                    "Invalid token at index " + position + ". Expected literal or '('.");
        }

        return new LiteralNode(next());
    }

    /**
     * Parses a character class inside brackets (e.g. [a-z0-9]).
     *
     * @return A CharClassNode representing the accepted set of characters.
     * @throws IllegalArgumentException if the bracket is never closed.
     */
    private RegexNode parseCharClass() {
        Set<Character> chars = new HashSet<>();

        while (hasNext() && peek() != ']') {
            if (match('\\')) {
                parseEscapedCharInClass(chars);
            } else {
                parseLiteralOrRangeInClass(chars);
            }
        }

        if (!match(']')) {
            throw new IllegalArgumentException("Missing closing bracket ']' for character class.");
        }

        return new CharClassNode(chars);
    }

    /**
     * Helper method to parse escaped characters (e.g. \d, \-) inside a character class.
     *
     * @param chars The set of characters being populated.
     */
    private void parseEscapedCharInClass(Set<Character> chars) {
        char escaped = next();
        if (escaped == 'd') {
            chars.addAll(getDigitSet());
        } else {
            chars.add(escaped);
        }
    }

    /**
     * Helper method to parse individual literals or character ranges (e.g. a-z) inside a character
     * class.
     *
     * @param chars The set of characters being populated.
     */
    private void parseLiteralOrRangeInClass(Set<Character> chars) {
        char startChar = next();

        // Check for range indicator (e.g. 'a-z')
        if (hasNext()
                && peek() == '-'
                && position + 1 < input.length()
                && input.charAt(position + 1) != ']') {
            match('-'); // Consume '-'
            char endChar = next();
            for (char c = startChar; c <= endChar; c++) {
                chars.add(c);
            }
        } else {
            chars.add(startChar);
        }
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

    private Set<Character> getDigitSet() {
        Set<Character> digits = new HashSet<>();
        for (char c = '0'; c <= '9'; c++) {
            digits.add(c);
        }
        return digits;
    }
}
