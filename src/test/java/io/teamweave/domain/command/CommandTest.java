package io.teamweave.domain.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.teamweave.domain.Students;
import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.IllegalTransitionException;
import io.teamweave.domain.team.Team;
import org.junit.jupiter.api.Test;

class CommandTest {

    @Test
    void moveExecutesUndoesAndRedoes() {
        Student ava = Students.student(1, "Ava");
        Team a = new Team("A");
        Team b = new Team("B");
        a.addMember(ava);

        CommandInvoker invoker = new CommandInvoker();
        invoker.execute(new MoveStudentCommand(ava, a, b));
        assertThat(b.contains(ava)).isTrue();
        assertThat(a.contains(ava)).isFalse();

        invoker.undo();
        assertThat(a.contains(ava)).isTrue();
        assertThat(b.contains(ava)).isFalse();

        invoker.redo();
        assertThat(b.contains(ava)).isTrue();
        assertThat(invoker.history()).containsExactly("move Ava from A to B");
    }

    @Test
    void aMoveIntoALockedTeamChangesNothing() {
        Student ava = Students.student(1, "Ava");
        Team a = new Team("A");
        Team b = new Team("B");
        a.addMember(ava);
        b.lock();

        assertThatThrownBy(() -> new MoveStudentCommand(ava, a, b).execute())
            .isInstanceOf(IllegalTransitionException.class);
        assertThat(a.contains(ava)).as("Ava must not vanish from both teams").isTrue();
    }

    @Test
    void aFailedRedoKeepsItsHistory() {
        Student ava = Students.student(1, "Ava");
        Team a = new Team("A");
        Team b = new Team("B");
        a.addMember(ava);

        CommandInvoker invoker = new CommandInvoker();
        invoker.execute(new MoveStudentCommand(ava, a, b));
        invoker.undo();
        b.lock();

        assertThatThrownBy(invoker::redo).isInstanceOf(IllegalTransitionException.class);
        assertThat(invoker.canRedo()).as("the failed step is not silently lost").isTrue();
        assertThat(a.contains(ava)).isTrue();
    }

    @Test
    void anewCommandClearsTheRedoHistory() {
        Student ava = Students.student(1, "Ava");
        Student ben = Students.student(2, "Ben");
        Team a = new Team("A");
        Team b = new Team("B");
        a.addMember(ava);
        a.addMember(ben);

        CommandInvoker invoker = new CommandInvoker();
        invoker.execute(new MoveStudentCommand(ava, a, b));
        invoker.undo();
        assertThat(invoker.canRedo()).isTrue();

        invoker.execute(new MoveStudentCommand(ben, a, b));
        assertThat(invoker.canRedo()).isFalse();
    }
}
