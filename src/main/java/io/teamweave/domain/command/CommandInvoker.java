package io.teamweave.domain.command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/** Runs commands and keeps undo/redo history. */
public class CommandInvoker {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }

    /** The applied commands, oldest first. */
    public List<String> history() {
        List<String> entries = new ArrayList<>();
        Iterator<Command> oldestFirst = undoStack.descendingIterator();
        while (oldestFirst.hasNext()) {
            entries.add(oldestFirst.next().describe());
        }
        return entries;
    }
}
