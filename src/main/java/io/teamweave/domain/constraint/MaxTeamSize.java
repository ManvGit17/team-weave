package io.teamweave.domain.constraint;

import io.teamweave.domain.formation.Assignment;

/** No team may exceed the given size. */
public record MaxTeamSize(int max) implements Constraint {

    @Override
    public boolean isSatisfiedBy(Assignment assignment) {
        return assignment.teams().stream().allMatch(team -> team.size() <= max);
    }

    @Override
    public String describe() {
        return "team size at most " + max;
    }
}
