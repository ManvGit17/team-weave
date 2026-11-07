package io.teamweave.domain.formation;

import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.Team;
import java.util.List;
import java.util.Optional;

/** A candidate placement of students into teams, which constraints evaluate. */
public record Assignment(List<Team> teams) {

    public Optional<Team> teamOf(Student student) {
        return teams.stream().filter(t -> t.contains(student)).findFirst();
    }

    public boolean together(Student a, Student b) {
        Optional<Team> ta = teamOf(a);
        Optional<Team> tb = teamOf(b);
        return ta.isPresent() && tb.isPresent() && ta.get() == tb.get();
    }

    public List<Student> allStudents() {
        return teams.stream().flatMap(t -> t.students().stream()).toList();
    }
}
