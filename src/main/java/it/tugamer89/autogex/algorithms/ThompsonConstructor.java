package it.tugamer89.autogex.algorithms;

import it.tugamer89.autogex.models.ENFA;
import it.tugamer89.autogex.regex.ast.RegexNode;
import it.tugamer89.autogex.regex.ast.RegexNode.*;

/**
 * Implements Thompson's Construction algorithm to convert an Abstract Syntax Tree (AST)
 * of a regular expression into an Epsilon-NFA (ENFA).
 */
public class ThompsonConstructor {

    private final ENFA.Builder builder;
    private int stateCounter;

    private ThompsonConstructor() {
        this.builder = new ENFA.Builder();
        this.stateCounter = 0;
    }

    /**
     * Converts a Regex AST into an ENFA.
     *
     * @param ast The root node of the parsed Regular Expression.
     * @return The constructed ENFA.
     */
    public static ENFA construct(RegexNode ast) {
        ThompsonConstructor constructor = new ThompsonConstructor();
        Fragment rootFragment = constructor.visit(ast);

        // Create a single global final state for the completed automaton
        String finalState = constructor.newStateName();
        constructor.builder.addState(finalState, true);
        
        // Connect the root fragment's accept state to the global final state
        constructor.builder.addEpsilonTransition(rootFragment.accept(), finalState);
        
        // Set the initial state
        constructor.builder.setInitialState(rootFragment.start());

        return constructor.builder.build();
    }

    /**
     * Represents a partially built ENFA with a single start and a single accept state.
     */
    private record Fragment(String start, String accept) {}

    private String newStateName() {
        return "q" + (stateCounter++);
    }

    private String addNonFinalState() {
        String name = newStateName();
        builder.addState(name, false);
        return name;
    }

    /**
     * Recursively visits the AST nodes and builds the corresponding ENFA fragments.
     */
    private Fragment visit(RegexNode node) {
        return switch (node) {
            case LiteralNode(char symbol) -> {
                String start = addNonFinalState();
                String accept = addNonFinalState();
                builder.addTransition(start, symbol, accept);
                yield new Fragment(start, accept);
            }
            case ConcatNode(RegexNode leftNode, RegexNode rightNode) -> {
                Fragment left = visit(leftNode);
                Fragment right = visit(rightNode);
                builder.addEpsilonTransition(left.accept(), right.start());
                yield new Fragment(left.start(), right.accept());
            }
            case UnionNode(RegexNode leftNode, RegexNode rightNode) -> {
                Fragment left = visit(leftNode);
                Fragment right = visit(rightNode);
                
                String start = addNonFinalState();
                String accept = addNonFinalState();
                
                builder.addEpsilonTransition(start, left.start());
                builder.addEpsilonTransition(start, right.start());
                builder.addEpsilonTransition(left.accept(), accept);
                builder.addEpsilonTransition(right.accept(), accept);
                
                yield new Fragment(start, accept);
            }
            case StarNode(RegexNode childNode) -> {
                Fragment child = visit(childNode);
                
                String start = addNonFinalState();
                String accept = addNonFinalState();
                
                builder.addEpsilonTransition(start, child.start());
                builder.addEpsilonTransition(start, accept); // Skip path (zero occurrences)
                builder.addEpsilonTransition(child.accept(), child.start()); // Loop path
                builder.addEpsilonTransition(child.accept(), accept);
                
                yield new Fragment(start, accept);
            }
            default -> throw new IllegalArgumentException("Unsupported RegexNode type: " + node.getClass());
        };
    }
}