package org.eu.autogex.regex.ast;

/**
 * Base interface for all nodes in the Regular Expression Abstract Syntax Tree (AST). Utilizes Java
 * Records to define immutable node implementations concisely.
 */
public interface RegexNode {

    /**
     * Accepts a visitor to apply an operation on this node.
     *
     * @param visitor The visitor applying the operation.
     * @param <T> The return type of the visitor.
     * @return The result of the visitor's operation.
     */
    <T> T accept(Visitor<T> visitor);

    /**
     * The Visitor interface for the AST nodes.
     *
     * @param <T> The return type of the visitor operations.
     */
    interface Visitor<T> {

        /**
         * Visits a LiteralNode.
         *
         * @param node The LiteralNode to visit.
         * @return The result of the visitor's operation.
         */
        T visit(LiteralNode node);

        /**
         * Visits a ConcatNode.
         *
         * @param node The ConcatNode to visit.
         * @return The result of the visitor's operation.
         */
        T visit(ConcatNode node);

        /**
         * Visits a UnionNode.
         *
         * @param node The UnionNode to visit.
         * @return The result of the visitor's operation.
         */
        T visit(UnionNode node);

        /**
         * Visits a StarNode.
         *
         * @param node The StarNode to visit.
         * @return The result of the visitor's operation.
         */
        T visit(StarNode node);
    }

    /**
     * Represents a single literal character in the regex (e.g., 'a', 'b').
     *
     * @param symbol The character symbol.
     */
    record LiteralNode(char symbol) implements RegexNode {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String toString() {
            return String.valueOf(symbol);
        }
    }

    /**
     * Represents the concatenation of two regular expressions (e.g., "ab"). Note: Concatenation is
     * usually implicit in regex strings.
     *
     * @param left The left operand of the concatenation.
     * @param right The right operand of the concatenation.
     */
    record ConcatNode(RegexNode left, RegexNode right) implements RegexNode {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String toString() {
            return left.toString() + right.toString();
        }
    }

    /**
     * Represents the union (OR) of two regular expressions (e.g., "a|b").
     *
     * @param left The left operand of the union.
     * @param right The right operand of the union.
     */
    record UnionNode(RegexNode left, RegexNode right) implements RegexNode {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String toString() {
            return "(" + left.toString() + "|" + right.toString() + ")";
        }
    }

    /**
     * Represents the Kleene Star operator, allowing zero or more repetitions (e.g., "a*").
     *
     * @param child The node to which the star operator is applied.
     */
    record StarNode(RegexNode child) implements RegexNode {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String toString() {
            if (child instanceof ConcatNode) {
                return "(" + child.toString() + ")*";
            }
            return child.toString() + "*";
        }
    }
}
