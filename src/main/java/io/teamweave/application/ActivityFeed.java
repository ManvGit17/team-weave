package io.teamweave.application;

import io.teamweave.domain.team.Team;
import io.teamweave.domain.team.TeamChangeListener;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * An Observer that records what happens to the teams, newest first, for the
 * activity feed on the board.
 */
public class ActivityFeed implements TeamChangeListener {

    public record Entry(LocalTime time, String text) {
    }

    private static final int LIMIT = 40;
    private final Deque<Entry> entries = new ArrayDeque<>();

    @Override
    public synchronized void onTeamChanged(Team team, String change) {
        note(team.name() + ": " + change);
    }

    public synchronized void note(String text) {
        entries.addFirst(new Entry(LocalTime.now().truncatedTo(ChronoUnit.SECONDS), text));
        while (entries.size() > LIMIT) {
            entries.removeLast();
        }
    }

    public synchronized List<Entry> entries() {
        return new ArrayList<>(entries);
    }

    public synchronized void clear() {
        entries.clear();
    }
}
