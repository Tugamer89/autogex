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

        private Fragment createEmptyFragment() {
            return new Fragment(addNonFinalState(), addNonFinalState());
        }

        @Override
        public Fragment visit(LiteralNode lit) {
            Fragment f = createEmptyFragment();
            builder.addTransition(f.start(), lit.symbol(), f.accept());
            return f;
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

            Fragment f = createEmptyFragment();

            builder.addEpsilonTransition(f.start(), left.start());
            builder.addEpsilonTransition(f.start(), right.start());
            builder.addEpsilonTransition(left.accept(), f.accept());
            builder.addEpsilonTransition(right.accept(), f.accept());

            return f;
        }

        @Override
        public Fragment visit(StarNode star) {
            Fragment child = star.child().accept(this);

            Fragment f = createEmptyFragment();

            builder.addEpsilonTransition(f.start(), child.start());
            builder.addEpsilonTransition(f.start(), f.accept()); // Skip path (zero occurrences)
            builder.addEpsilonTransition(child.accept(), child.start()); // Loop path
            builder.addEpsilonTransition(child.accept(), f.accept());

            return f;
        }

        @Override
        public Fragment visit(PlusNode plus) {
            Fragment child = plus.child().accept(this);

            Fragment f = createEmptyFragment();

            builder.addEpsilonTransition(f.start(), child.start());
            builder.addEpsilonTransition(child.accept(), child.start()); // Loop path (one or more)
            builder.addEpsilonTransition(child.accept(), f.accept());

            return f;
        }

        @Override
        public Fragment visit(OptionalNode opt) {
            Fragment child = opt.child().accept(this);

            Fragment f = createEmptyFragment();

            builder.addEpsilonTransition(f.start(), child.start());
            builder.addEpsilonTransition(f.start(), f.accept()); // Skip path (zero occurrences)
            builder.addEpsilonTransition(child.accept(), f.accept());

            return f;
        }

        @Override
        public Fragment visit(CharClassNode charClass) {
            Fragment f = createEmptyFragment();

            for (char c : charClass.chars()) {
                builder.addTransition(f.start(), c, f.accept());
            }

            return f;
        }

        @Override
        public Fragment visit(WildcardNode wildcard) {
            Fragment f = createEmptyFragment();

            // Cover standard ASCII printable characters for the wildcard '.'
            for (char c = 32; c <= 126; c++) {
                builder.addTransition(f.start(), c, f.accept());
            }

            return f;
        }
    }
}
