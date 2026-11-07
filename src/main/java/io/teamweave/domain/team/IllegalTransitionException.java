package io.teamweave.domain.team;

/** Thrown when a team is asked to make a transition its current state forbids. */
public class IllegalTransitionException extends RuntimeException {

    public IllegalTransitionException(String action, String fromState) {
        super("cannot " + action + " a team that is " + fromState);
    }
}
