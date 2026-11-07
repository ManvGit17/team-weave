package io.teamweave.domain.formation;

import io.teamweave.domain.team.Team;
import java.util.List;

/** The outcome of running a strategy: the teams, the score, why, and any
 *  constraints the result violated. */
public record MatchResult(String strategy, List<Team> teams, Score score,
                          List<Explanation> explanations, List<String> violations) {

    public boolean satisfiesAllConstraints() {
        return violations.isEmpty();
    }
}
