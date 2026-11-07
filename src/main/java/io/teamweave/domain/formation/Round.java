package io.teamweave.domain.formation;

import io.teamweave.domain.constraint.Constraint;
import io.teamweave.domain.student.Student;
import java.util.List;

/** A formation round: the candidates, the target team size, and the constraints. */
public record Round(List<Student> candidates, int targetTeamSize, List<Constraint> constraints) {

    public Round {
        if (targetTeamSize < 1) {
            throw new IllegalArgumentException("targetTeamSize must be >= 1");
        }
        candidates = List.copyOf(candidates);
        constraints = List.copyOf(constraints);
    }

    public Round(List<Student> candidates, int targetTeamSize) {
        this(candidates, targetTeamSize, List.of());
    }

    /** Number of teams needed to hold the candidates at the target size. */
    public int teamCount() {
        return Math.max(1, (int) Math.ceil((double) candidates.size() / targetTeamSize));
    }
}
