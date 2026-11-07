package io.teamweave.domain.command;

/** A reversible action on the teams — the Command pattern. */
public interface Command {

    void execute();

    void undo();

    String describe();
}
