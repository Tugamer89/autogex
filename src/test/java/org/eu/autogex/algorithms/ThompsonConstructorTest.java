package org.eu.autogex.algorithms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.eu.autogex.regex.ast.RegexNode;

class ThompsonConstructorTest {

    @Test
    void testThrowsExceptionOnUnsupportedNode() {
        // Create an anonymous class that implements RegexNode
        RegexNode unsupportedNode = new RegexNode() {
            @Override
            public <T> T accept(Visitor<T> visitor) {
                throw new IllegalArgumentException("Unsupported RegexNode type");
            }
            @Override
            public String toString() {
                return "UnsupportedDummyNode";
            }
        };

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ThompsonConstructor.construct(unsupportedNode);
        });

        // Verify that the correct exception is thrown by the default switch branch
        assertTrue(exception.getMessage().contains("Unsupported RegexNode type"), 
            "The constructor should reject unknown AST node implementations");
    }
}