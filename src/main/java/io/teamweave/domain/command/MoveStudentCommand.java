package io.teamweave.domain.command;

import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.IllegalTransitionException;
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
        requireBothOpen();
        from.removeMember(student);
        to.addMember(student);
    }

    @Override
    public void undo() {
        requireBothOpen();
        to.removeMember(student);
        from.addMember(student);
    }

    /**
     * Check both teams before touching either, so a move never half-happens
     * (the student leaving one team but being refused by the other).
     */
    private void requireBothOpen() {
        for (Team team : new Team[] {from, to}) {
            if (!team.state().allowsMembershipChanges()) {
                throw new IllegalTransitionException("change the members of", team.state().label());
            }
        }
    }

    @Override
    public String describe() {
        return "move " + student.getName() + " from " + from.name() + " to " + to.name();
    }
}
