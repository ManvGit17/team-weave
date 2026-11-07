package io.teamweave.application;

import io.teamweave.domain.team.Team;
import io.teamweave.domain.team.TeamChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A concrete Observer that logs every team change. */
public class LoggingTeamListener implements TeamChangeListener {

    private static final Logger log = LoggerFactory.getLogger(LoggingTeamListener.class);

    @Override
    public void onTeamChanged(Team team, String change) {
        log.info("{}: {} (now {} members, {})",
            team.name(), change, team.size(), team.state().label());
    }
}
