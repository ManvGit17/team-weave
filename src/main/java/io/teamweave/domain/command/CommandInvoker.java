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

    /** Undo the latest command. If it fails, the history is left untouched. */
    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        Command command = undoStack.peek();
        command.undo();
        undoStack.pop();
        redoStack.push(command);
    }

    /** Redo the latest undone command. If it fails, the history is left untouched. */
    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        Command command = redoStack.peek();
        command.execute();
        redoStack.pop();
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
