package io.teamweave.domain.team;

/** Observer: notified whenever a team's membership or state changes. */
@FunctionalInterface
public interface TeamChangeListener {

    void onTeamChanged(Team team, String change);
}
