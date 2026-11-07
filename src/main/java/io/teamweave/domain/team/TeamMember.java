package io.teamweave.domain.team;

import io.teamweave.domain.student.Role;
import io.teamweave.domain.student.Student;

/** A student's place on a team, optionally in a specific role. */
public record TeamMember(Student student, Role role) {

    public TeamMember(Student student) {
        this(student, null);
    }
}
