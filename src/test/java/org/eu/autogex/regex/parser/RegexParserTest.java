package org.eu.autogex.regex.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.eu.autogex.regex.ast.RegexNode;
import org.eu.autogex.regex.ast.RegexNode.*;
import org.junit.jupiter.api.Test;

class RegexParserTest {

    @Test
    void testParseLiteral() {
        RegexNode node = RegexParser.parse("a");
        assertInstanceOf(LiteralNode.class, node);
        assertEquals('a', ((LiteralNode) node).symbol());
    }

    @Test
    void testParseConcatenation() {
        RegexNode node = RegexParser.parse("ab");
        assertInstanceOf(ConcatNode.class, node);

        ConcatNode concat = (ConcatNode) node;
        assertInstanceOf(LiteralNode.class, concat.left());
        assertInstanceOf(LiteralNode.class, concat.right());
        assertEquals("ab", concat.toString());
    }

    @Test
    void testParseUnion() {
        RegexNode node = RegexParser.parse("a|b");
        assertInstanceOf(UnionNode.class, node);

        UnionNode union = (UnionNode) node;
        assertEquals("(a|b)", union.toString());
    }

    @Test
    void testParseStar() {
        RegexNode node = RegexParser.parse("a*");
        assertInstanceOf(StarNode.class, node);

        StarNode star = (StarNode) node;
        assertInstanceOf(LiteralNode.class, star.child());
        assertEquals("a*", star.toString());
    }

    @Test
    void testParseStarWithConcatenation() {
        // Explicitly tests the 'instanceof ConcatNode' logic in StarNode.toString()
        // We expect parentheses to be added correctly: (ab)*
        RegexNode node = RegexParser.parse("(ab)*");
        assertInstanceOf(StarNode.class, node);
        assertEquals("(ab)*", node.toString());
    }

    @Test
    void testParseStarWithUnion() {
        // Verify that UnionNode manages its own parentheses so StarNode doesn't add double ones
        // Expected result: (a|b)* and NOT ((a|b))*
        RegexNode node = RegexParser.parse("(a|b)*");
        assertInstanceOf(StarNode.class, node);
        assertEquals("(a|b)*", node.toString());
    }

    @Test
    void testParseComplexExpression() {
        // Tests operator precedence: Star > Concat > Union
        // Target: (a|b)*abb
        RegexNode node = RegexParser.parse("(a|b)*abb");

        // Root should be a concatenation of (a|b)*ab and b
        assertInstanceOf(ConcatNode.class, node);

        // The toString method of our AST natively rebuilds the regex
        assertEquals("(a|b)*abb", node.toString());
    }

    @Test
    void testParseHandlesWhitespaces() {
        RegexNode node1 = RegexParser.parse("a | b");
        RegexNode node2 = RegexParser.parse("a|b");

        assertEquals(node2.toString(), node1.toString(), "Parser should ignore whitespaces");
    }

    @Test
    void testThrowsExceptionOnEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> RegexParser.parse(""));
        assertThrows(IllegalArgumentException.class, () -> RegexParser.parse(null));
    }

    @Test
    void testThrowsExceptionOnMissingParenthesis() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> RegexParser.parse("(a|b"));
        assertTrue(
                exception.getMessage().contains("Missing closing parenthesis"),
                "Should detect missing closing parenthesis");
    }

    @Test
    void testThrowsExceptionOnInvalidToken() {
        IllegalArgumentException exception1 =
                assertThrows(IllegalArgumentException.class, () -> RegexParser.parse("*a"));
        assertTrue(
                exception1.getMessage().contains("Invalid token"), "Should detect unexpected star");

        IllegalArgumentException exception2 =
                assertThrows(IllegalArgumentException.class, () -> RegexParser.parse("a|*b"));
        assertTrue(
                exception2.getMessage().contains("Invalid token"),
                "Should detect invalid sequence");
    }

    @Test
    void testThrowsExceptionOnUnbalancedParenthesisClosing() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> RegexParser.parse("a)"));
        assertTrue(
                exception.getMessage().contains("Unexpected character"),
                "Should detect orphaned closing parenthesis");
    }
}
