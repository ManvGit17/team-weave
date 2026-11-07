package io.teamweave.domain.matching;

import io.teamweave.domain.constraint.Constraint;
import io.teamweave.domain.formation.Assignment;
import io.teamweave.domain.formation.Explanation;
import io.teamweave.domain.formation.MatchResult;
import io.teamweave.domain.formation.Round;
import io.teamweave.domain.formation.Score;
import io.teamweave.domain.formation.Scorer;
import io.teamweave.domain.team.Team;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared machinery for strategies: subclasses only place students into teams;
 * the base scores the result and records any violated constraints.
 */
public abstract class AbstractMatchingStrategy implements MatchingStrategy {

    private final Scorer scorer = new Scorer();

    protected abstract List<Team> placeStudents(Round round, List<Explanation> explanations);

    @Override
    public MatchResult assign(Round round) {
        List<Explanation> explanations = new ArrayList<>();
        List<Team> teams = placeStudents(round, explanations);

        Score score = scorer.score(round, teams);

        Assignment assignment = new Assignment(teams);
        List<String> violations = new ArrayList<>();
        for (Constraint constraint : round.constraints()) {
            if (!constraint.isSatisfiedBy(assignment)) {
                violations.add(constraint.describe());
            }
        }
        return new MatchResult(name(), teams, score, explanations, violations);
    }

    protected List<Team> emptyTeams(Round round) {
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < round.teamCount(); i++) {
            teams.add(new Team("Team " + (i + 1)));
        }
        return teams;
    }
}
