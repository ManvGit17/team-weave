package io.teamweave.domain.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import io.teamweave.domain.Students;
import io.teamweave.domain.formation.Assignment;
import io.teamweave.domain.student.Role;
import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.Team;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConstraintTest {

    private final Student ava = Students.wanting(1, "Ava", Role.BACKEND, "java", 4);
    private final Student ben = Students.wanting(2, "Ben", Role.FRONTEND, "react", 4);
    private final Student cara = Students.student(3, "Cara");

    private Assignment twoTeams() {
        Team one = new Team("Team 1");
        one.addMember(ava);
        one.addMember(ben);
        Team two = new Team("Team 2");
        two.addMember(cara);
        return new Assignment(List.of(one, two));
    }

    @Test
    void maxTeamSizeIsRespectedOrNot() {
        assertThat(new MaxTeamSize(2).isSatisfiedBy(twoTeams())).isTrue();
        assertThat(new MaxTeamSize(1).isSatisfiedBy(twoTeams())).isFalse();
    }

    @Test
    void keepTogetherAndKeepApart() {
        assertThat(new KeepTogether(ava, ben).isSatisfiedBy(twoTeams())).isTrue();
        assertThat(new KeepTogether(ava, cara).isSatisfiedBy(twoTeams())).isFalse();
        assertThat(new KeepApart(ava, cara).isSatisfiedBy(twoTeams())).isTrue();
        assertThat(new KeepApart(ava, ben).isSatisfiedBy(twoTeams())).isFalse();
    }

    @Test
    void requiredRolesChecksEveryTeam() {
        // Team 1 covers BACKEND+FRONTEND; Team 2 (Cara) covers nothing.
        Constraint needsBackend = new RequiredRoles(Set.of(Role.BACKEND));
        assertThat(needsBackend.isSatisfiedBy(twoTeams())).isFalse();
    }

    @Test
    void constraintsCompose() {
        Constraint fits = new MaxTeamSize(2);
        Constraint together = new KeepTogether(ava, ben);
        assertThat(fits.and(together).isSatisfiedBy(twoTeams())).isTrue();
        assertThat(fits.and(new KeepApart(ava, ben)).isSatisfiedBy(twoTeams())).isFalse();
        assertThat(new MaxTeamSize(1).or(together).isSatisfiedBy(twoTeams())).isTrue();
        assertThat(together.negate().isSatisfiedBy(twoTeams())).isFalse();
    }
}
