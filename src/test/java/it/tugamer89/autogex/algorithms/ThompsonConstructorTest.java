package it.tugamer89.autogex.algorithms;

import it.tugamer89.autogex.regex.ast.RegexNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThompsonConstructorTest {

    @Test
    void testThrowsExceptionOnUnsupportedNode() {
        // Create an anonymous class that implements RegexNode 
        // but is not one of the supported records (Literal, Concat, Union, Star)
        RegexNode unsupportedNode = new RegexNode() {
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