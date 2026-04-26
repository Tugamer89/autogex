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
    void testParsePlus() {
        RegexNode node = RegexParser.parse("a+");
        assertInstanceOf(PlusNode.class, node);

        PlusNode plus = (PlusNode) node;
        assertInstanceOf(LiteralNode.class, plus.child());
        assertEquals("a+", plus.toString());
    }

    @Test
    void testParseOptional() {
        RegexNode node = RegexParser.parse("a?");
        assertInstanceOf(OptionalNode.class, node);

        OptionalNode opt = (OptionalNode) node;
        assertInstanceOf(LiteralNode.class, opt.child());
        assertEquals("a?", opt.toString());
    }

    @Test
    void testParseWildcard() {
        RegexNode node = RegexParser.parse(".");
        assertInstanceOf(WildcardNode.class, node);
        assertEquals(".", node.toString());
    }

    @Test
    void testParseCharClassRange() {
        RegexNode node = RegexParser.parse("[a-c]");
        assertInstanceOf(CharClassNode.class, node);

        CharClassNode charClass = (CharClassNode) node;
        assertEquals(3, charClass.chars().size(), "Should contain exactly 'a', 'b', and 'c'");
        assertTrue(charClass.chars().contains('a'));
        assertTrue(charClass.chars().contains('b'));
        assertTrue(charClass.chars().contains('c'));
    }

    @Test
    void testParseCharClassDigitEscape() {
        RegexNode node = RegexParser.parse("\\d");
        assertInstanceOf(CharClassNode.class, node);

        CharClassNode charClass = (CharClassNode) node;
        assertEquals(10, charClass.chars().size(), "Should contain exactly 10 digits");
        assertTrue(charClass.chars().contains('0'));
        assertTrue(charClass.chars().contains('9'));
    }

    @Test
    void testParseCharClassMultipleRanges() {
        RegexNode node = RegexParser.parse("[a-cx-z\\d]");
        assertInstanceOf(CharClassNode.class, node);

        CharClassNode charClass = (CharClassNode) node;
        assertTrue(charClass.chars().contains('b'));
        assertTrue(charClass.chars().contains('y'));
        assertTrue(charClass.chars().contains('5'));
    }

    @Test
    void testParseCharClassWithEscapedCharacters() {
        RegexNode node = RegexParser.parse("[\\-\\]\\\\]");
        assertInstanceOf(CharClassNode.class, node);

        CharClassNode charClass = (CharClassNode) node;
        assertTrue(charClass.chars().contains('-'), "Should contain escaped dash");
        assertTrue(charClass.chars().contains(']'), "Should contain escaped closing bracket");
        assertTrue(charClass.chars().contains('\\'), "Should contain escaped backslash");
    }

    @Test
    void testParseCharClassWithLiteralDashAtEnd() {
        RegexNode node = RegexParser.parse("[a-]");
        assertInstanceOf(CharClassNode.class, node);

        CharClassNode charClass = (CharClassNode) node;
        assertTrue(charClass.chars().contains('a'), "Should contain 'a'");
        assertTrue(charClass.chars().contains('-'), "Should treat dash at the end as literal");
    }

    @Test
    void testParseEscapedLiteralsOutsideCharClass() {
        RegexNode node = RegexParser.parse("\\*\\+\\?\\(\\)\\|");
        assertInstanceOf(ConcatNode.class, node);

        // The AST should stringify this back to the raw concatenation of literal nodes
        assertEquals("*+?()|", node.toString());
    }

    @Test
    void testParseStarWithConcatenation() {
        RegexNode node = RegexParser.parse("(ab)*");
        assertInstanceOf(StarNode.class, node);
        assertEquals("(ab)*", node.toString());
    }

    @Test
    void testParseStarWithUnion() {
        RegexNode node = RegexParser.parse("(a|b)*");
        assertInstanceOf(StarNode.class, node);
        assertEquals("(a|b)*", node.toString());
    }

    @Test
    void testParseComplexExpression() {
        RegexNode node = RegexParser.parse("(a|b)*a+b?c.");
        assertInstanceOf(ConcatNode.class, node);
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
    void testThrowsExceptionOnMissingBracket() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> RegexParser.parse("[a-z"));
        assertTrue(
                exception.getMessage().contains("Missing closing bracket"),
                "Should detect missing closing bracket");
    }

    @Test
    void testThrowsExceptionOnIncompleteRange() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> RegexParser.parse("[a-"));
        assertTrue(
                exception.getMessage().contains("Missing closing bracket"),
                "Should detect missing closing bracket for incomplete range");
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
