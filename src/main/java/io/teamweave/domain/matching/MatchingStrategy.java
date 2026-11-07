package io.teamweave.domain.matching;

import io.teamweave.domain.formation.MatchResult;
import io.teamweave.domain.formation.Round;

/**
 * An interchangeable team-formation algorithm — the Strategy pattern. Each
 * strategy assigns a round's candidates to teams, scores the result and
 * explains its placements.
 */
public interface MatchingStrategy {

    String name();

    MatchResult assign(Round round);
}
