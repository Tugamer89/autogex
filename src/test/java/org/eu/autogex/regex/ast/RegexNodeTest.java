package org.eu.autogex.regex.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

class RegexNodeTest {

    @Test
    void testLiteralNodeToString() {
        RegexNode node = new RegexNode.LiteralNode('a');
        assertEquals("a", node.toString());
    }

    @Test
    void testConcatNodeToString() {
        RegexNode left = new RegexNode.LiteralNode('a');
        RegexNode right = new RegexNode.LiteralNode('b');
        RegexNode node = new RegexNode.ConcatNode(left, right);
        assertEquals("ab", node.toString());
    }

    @Test
    void testUnionNodeToString() {
        RegexNode left = new RegexNode.LiteralNode('a');
        RegexNode right = new RegexNode.LiteralNode('b');
        RegexNode node = new RegexNode.UnionNode(left, right);
        assertEquals("(a|b)", node.toString());
    }

    @Test
    void testStarNodeToString() {
        RegexNode child = new RegexNode.LiteralNode('a');
        RegexNode node = new RegexNode.StarNode(child);
        assertEquals("a*", node.toString());

        RegexNode concatChild =
                new RegexNode.ConcatNode(
                        new RegexNode.LiteralNode('a'), new RegexNode.LiteralNode('b'));
        RegexNode concatStar = new RegexNode.StarNode(concatChild);
        assertEquals("(ab)*", concatStar.toString());
    }

    @Test
    void testPlusNodeToString() {
        RegexNode child = new RegexNode.LiteralNode('a');
        RegexNode node = new RegexNode.PlusNode(child);
        assertEquals("a+", node.toString());

        RegexNode concatChild =
                new RegexNode.ConcatNode(
                        new RegexNode.LiteralNode('a'), new RegexNode.LiteralNode('b'));
        RegexNode concatPlus = new RegexNode.PlusNode(concatChild);
        assertEquals("(ab)+", concatPlus.toString());
    }

    @Test
    void testOptionalNodeToString() {
        RegexNode child = new RegexNode.LiteralNode('a');
        RegexNode node = new RegexNode.OptionalNode(child);
        assertEquals("a?", node.toString());

        RegexNode concatChild =
                new RegexNode.ConcatNode(
                        new RegexNode.LiteralNode('a'), new RegexNode.LiteralNode('b'));
        RegexNode concatOpt = new RegexNode.OptionalNode(concatChild);
        assertEquals("(ab)?", concatOpt.toString());
    }

    @Test
    void testCharClassNodeToString() {
        RegexNode node = new RegexNode.CharClassNode(Set.of('a', 'b', 'c'));
        assertEquals("[abc]", node.toString());
    }

    @Test
    void testWildcardNodeToString() {
        RegexNode node = new RegexNode.WildcardNode();
        assertEquals(".", node.toString());
    }
}
