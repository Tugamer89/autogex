package org.eu.autogex.algorithms;

import org.eu.autogex.models.ENFA;
import org.eu.autogex.regex.ast.RegexNode;
import org.eu.autogex.regex.ast.RegexNode.*;

/**
 * Implements Thompson's Construction algorithm to convert an Abstract Syntax Tree (AST) of a
 * regular expression into an Epsilon-NFA (ENFA).
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
        Fragment rootFragment = constructor.generateFragment(ast);

        // Create a single global final state for the completed automaton
        String finalState = constructor.newStateName();
        constructor.builder.addState(finalState, true);

        // Connect the root fragment's accept state to the global final state
        constructor.builder.addEpsilonTransition(rootFragment.accept(), finalState);

        // Set the initial state
        constructor.builder.setInitialState(rootFragment.start());

        return constructor.builder.build();
    }

    /** Represents a partially built ENFA with a single start and a single accept state. */
    private record Fragment(String start, String accept) {}

    private String newStateName() {
        return "q" + (stateCounter++);
    }

    private Fragment generateFragment(RegexNode node) {
        return node.accept(new AstVisitor());
    }

    /** Visitor implementation to traverse the AST and build ENFA fragments using Polymorphism. */
    private class AstVisitor implements RegexNode.Visitor<Fragment> {

        private String addNonFinalState() {
            String name = newStateName();
            builder.addState(name, false);
            return name;
        }

        @Override
        public Fragment visit(LiteralNode lit) {
            String start = addNonFinalState();
            String accept = addNonFinalState();
            builder.addTransition(start, lit.symbol(), accept);
            return new Fragment(start, accept);
        }

        @Override
        public Fragment visit(ConcatNode concat) {
            Fragment left = concat.left().accept(this);
            Fragment right = concat.right().accept(this);
            builder.addEpsilonTransition(left.accept(), right.start());
            return new Fragment(left.start(), right.accept());
        }

        @Override
        public Fragment visit(UnionNode union) {
            Fragment left = union.left().accept(this);
            Fragment right = union.right().accept(this);

            String start = addNonFinalState();
            String accept = addNonFinalState();

            builder.addEpsilonTransition(start, left.start());
            builder.addEpsilonTransition(start, right.start());
            builder.addEpsilonTransition(left.accept(), accept);
            builder.addEpsilonTransition(right.accept(), accept);

            return new Fragment(start, accept);
        }

        @Override
        public Fragment visit(StarNode star) {
            Fragment child = star.child().accept(this);

            String start = addNonFinalState();
            String accept = addNonFinalState();

            builder.addEpsilonTransition(start, child.start());
            builder.addEpsilonTransition(start, accept); // Skip path (zero occurrences)
            builder.addEpsilonTransition(child.accept(), child.start()); // Loop path
            builder.addEpsilonTransition(child.accept(), accept);

            return new Fragment(start, accept);
        }
    }
}
