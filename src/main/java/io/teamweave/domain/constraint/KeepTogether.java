package io.teamweave.domain.constraint;

import io.teamweave.domain.formation.Assignment;
import io.teamweave.domain.student.Student;

/** Two students must end up on the same team. */
public record KeepTogether(Student first, Student second) implements Constraint {

    @Override
    public boolean isSatisfiedBy(Assignment assignment) {
        return assignment.together(first, second);
    }

    @Override
    public String describe() {
        return first.getName() + " with " + second.getName();
    }
}
