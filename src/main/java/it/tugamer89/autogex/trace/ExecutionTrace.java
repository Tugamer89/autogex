package it.tugamer89.autogex.trace;

import java.util.List;

/**
 * Contains the complete chronological sequence of states evaluated by an automaton
 * when processing a specific input string.
 */
public class ExecutionTrace {

    private static final String SEPARATOR = "=========================================%n";
    private final String input;
    private final List<ExecutionStep> steps;
    private final boolean accepted;

    /**
     * Constructs a new ExecutionTrace.
     *
     * @param input    The original string evaluated.
     * @param steps    The ordered list of execution steps.
     * @param accepted The final evaluation outcome.
     */
    public ExecutionTrace(String input, List<ExecutionStep> steps, boolean accepted) {
        this.input = input;
        this.steps = List.copyOf(steps);
        this.accepted = accepted;
    }

    /**
     * @return The original input string.
     */
    public String getInput() {
        return input;
    }

    /**
     * @return An immutable list containing all evaluation steps.
     */
    public List<ExecutionStep> getSteps() {
        return steps;
    }

    /**
     * @return True if the automaton successfully accepted the string.
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * Generates a beautifully formatted multiline string mapping the entire execution path.
     * Useful for debugging and console outputs.
     *
     * @return The formatted trace log.
     */
    public String getFormattedTrace() {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR);
        sb.append(String.format(" TRACE FOR INPUT: '%s'%n", input));
        sb.append(SEPARATOR);
        
        for (int i = 0; i < steps.size(); i++) {
            sb.append(String.format(" Step %02d | %s%n", i, steps.get(i).toString()));
        }
        
        sb.append(SEPARATOR);
        sb.append(String.format(" RESULT  | %s%n", accepted ? "ACCEPTED" : "REJECTED"));
        sb.append(SEPARATOR);
        
        return sb.toString();
    }
}