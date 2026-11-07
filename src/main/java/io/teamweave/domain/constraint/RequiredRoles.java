package io.teamweave.domain.constraint;

import io.teamweave.domain.formation.Assignment;
import io.teamweave.domain.student.Role;
import io.teamweave.domain.team.Team;
import io.teamweave.domain.team.TeamMember;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Every team must include students who want each of the required roles. */
public record RequiredRoles(Set<Role> roles) implements Constraint {

    @Override
    public boolean isSatisfiedBy(Assignment assignment) {
        return assignment.teams().stream().allMatch(this::covers);
    }

    private boolean covers(Team team) {
        Set<Role> covered = team.members().stream()
            .map(TeamMember::student)
            .flatMap(s -> s.getDesiredRoles().stream())
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
        return covered.containsAll(roles);
    }

    @Override
    public String describe() {
        return "each team covers roles " + roles;
    }
}
