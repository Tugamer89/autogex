package it.tugamer89.autogex.regex;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegexTest {

    @Test
    void testRegexLiteral() {
        Regex regex = new Regex("abc");
        
        assertEquals("abc", regex.getPattern());
        assertTrue(regex.matches("abc"), "Should match the exact literal string");
        assertFalse(regex.matches("ab"), "Should reject partial matches");
        assertFalse(regex.matches("abcd"), "Should reject strings with extra characters");
    }

    @Test
    void testRegexUnion() {
        Regex regex = new Regex("a|b");
        
        assertTrue(regex.matches("a"), "Should match left side of union");
        assertTrue(regex.matches("b"), "Should match right side of union");
        assertFalse(regex.matches("ab"), "Should reject concatenation when union is expected");
        assertFalse(regex.matches("c"), "Should reject unrelated characters");
    }

    @Test
    void testRegexKleeneStar() {
        Regex regex = new Regex("a*");
        
        assertTrue(regex.matches(""), "Star operator must accept empty string");
        assertTrue(regex.matches("a"), "Star operator must accept single occurrence");
        assertTrue(regex.matches("aaaaa"), "Star operator must accept multiple occurrences");
        assertFalse(regex.matches("b"), "Should reject unrelated characters");
        assertFalse(regex.matches("aaab"), "Should reject strings ending with unrelated characters");
    }

    @Test
    void testComplexRegexPipeline() {
        // This tests the entire parser, constructor, converter, and minimizer pipeline!
        Regex regex = new Regex("(a|b)*abb");

        // Accepted strings (must end with "abb")
        assertTrue(regex.matches("abb"));
        assertTrue(regex.matches("aabb"));
        assertTrue(regex.matches("babb"));
        assertTrue(regex.matches("abaabbabaabb"));

        // Rejected strings
        assertFalse(regex.matches("ab"), "Too short");
        assertFalse(regex.matches("abba"), "Does not end with abb");
        assertFalse(regex.matches(""), "Empty string cannot end with abb");
        assertFalse(regex.matches("bbbb"), "Does not contain a");
    }
    
    @Test
    void testRegexOptimizationExcellence() {
        Regex regex = new Regex("(a|b)*abb");
        
        // Theoretical knowledge: the minimal DFA for "(a|b)*abb" requires EXACTLY 4 states.
        assertEquals(4, regex.getAutomaton().getStates().size(), 
                "The minimized DFA for (a|b)*abb must have exactly 4 states");
    }
}