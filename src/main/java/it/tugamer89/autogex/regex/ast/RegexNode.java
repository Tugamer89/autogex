package it.tugamer89.autogex.regex.ast;

/**
 * Base interface for all nodes in the Regular Expression Abstract Syntax Tree (AST).
 * Utilizes Java Records to define immutable node implementations concisely.
 */
public interface RegexNode {

    /**
     * Represents a single literal character in the regex (e.g., 'a', 'b').
     */
    record LiteralNode(char symbol) implements RegexNode {
        @Override
        public String toString() {
            return String.valueOf(symbol);
        }
    }

    /**
     * Represents the concatenation of two regular expressions (e.g., "ab").
     * Note: Concatenation is usually implicit in regex strings.
     */
    record ConcatNode(RegexNode left, RegexNode right) implements RegexNode {
        @Override
        public String toString() {
            return left.toString() + right.toString();
        }
    }

    /**
     * Represents the union (OR) of two regular expressions (e.g., "a|b").
     */
    record UnionNode(RegexNode left, RegexNode right) implements RegexNode {
        @Override
        public String toString() {
            return "(" + left.toString() + "|" + right.toString() + ")";
        }
    }

    /**
     * Represents the Kleene Star operator, allowing zero or more repetitions (e.g., "a*").
     */
    record StarNode(RegexNode child) implements RegexNode {
        @Override
        public String toString() {
            if (child instanceof ConcatNode) {
                return "(" + child.toString() + ")*";
            }
            return child.toString() + "*";
        }
    }
}