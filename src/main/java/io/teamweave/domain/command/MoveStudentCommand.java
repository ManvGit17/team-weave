package io.teamweave.domain.command;

import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.Team;

/** Move a student from one team to another, reversibly. */
public class MoveStudentCommand implements Command {

    private final Student student;
    private final Team from;
    private final Team to;

    public MoveStudentCommand(Student student, Team from, Team to) {
        this.student = student;
        this.from = from;
        this.to = to;
    }

    @Override
    public void execute() {
        from.removeMember(student);
        to.addMember(student);
    }

    @Override
    public void undo() {
        to.removeMember(student);
        from.addMember(student);
    }

    @Override
    public String describe() {
        return "move " + student.getName() + " from " + from.name() + " to " + to.name();
    }
}
