package io.teamweave.application;

import io.teamweave.domain.command.CommandInvoker;
import io.teamweave.domain.command.MoveStudentCommand;
import io.teamweave.domain.constraint.Constraint;
import io.teamweave.domain.formation.Assignment;
import io.teamweave.domain.formation.Explanation;
import io.teamweave.domain.formation.MatchResult;
import io.teamweave.domain.formation.Round;
import io.teamweave.domain.formation.Score;
import io.teamweave.domain.formation.Scorer;
import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.IllegalTransitionException;
import io.teamweave.domain.team.Team;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * The board a coordinator is working on: the current teams, moves made through
 * commands (with undo and redo), lifecycle changes, and an activity feed that
 * observes every team. One per browser session.
 */
@Component
@SessionScope
public class FormationSession {

    private final Scorer scorer = new Scorer();
    private final ActivityFeed feed = new ActivityFeed();
    private CommandInvoker invoker = new CommandInvoker();

    private Round round;
    private String strategy;
    private List<Team> teams = List.of();
    private List<Explanation> explanations = List.of();

    public synchronized void start(Round round, MatchResult result) {
        this.round = round;
        this.strategy = result.strategy();
        this.teams = result.teams();
        this.explanations = result.explanations();
        this.invoker = new CommandInvoker();
        feed.clear();
        teams.forEach(team -> team.addListener(feed));
        feed.note("Formed " + teams.size() + " teams with " + strategy);
    }

    public synchronized boolean hasFormation() {
        return round != null;
    }

    public synchronized void clear() {
        round = null;
        strategy = null;
        teams = List.of();
        explanations = List.of();
        invoker = new CommandInvoker();
        feed.clear();
    }

    // -- Commands -----------------------------------------------------------
    /** Move a student to another team, through an undoable command. */
    public synchronized void move(long studentId, String toTeamName) {
        Team to = team(toTeamName);
        Team from = teams.stream()
            .filter(t -> t.students().stream().anyMatch(s -> idOf(s) == studentId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("no such student on the board"));
        if (from == to) {
            return;
        }
        for (Team t : List.of(from, to)) {
            if (!t.state().allowsMembershipChanges()) {
                throw new IllegalTransitionException("change the members of", t.state().label());
            }
        }
        Student student = from.students().stream()
            .filter(s -> idOf(s) == studentId).findFirst().orElseThrow();
        invoker.execute(new MoveStudentCommand(student, from, to));
    }

    public synchronized void undo() {
        invoker.undo();
    }

    public synchronized void redo() {
        invoker.redo();
    }

    public synchronized boolean canUndo() {
        return invoker.canUndo();
    }

    public synchronized boolean canRedo() {
        return invoker.canRedo();
    }

    public synchronized List<String> history() {
        return invoker.history();
    }

    // -- Lifecycle (State) ----------------------------------------------------
    /**
     * Advance a team through its lifecycle. Once a team leaves "forming" its
     * membership is fixed, so the move history is cleared rather than leaving
     * undo steps that could no longer be applied.
     */
    public synchronized void transition(String teamName, String action) {
        Team team = team(teamName);
        switch (action) {
            case "lock" -> team.lock();
            case "activate" -> team.activate();
            case "submit" -> team.submit();
            case "review" -> team.review();
            default -> throw new IllegalArgumentException("unknown action: " + action);
        }
        if (!team.state().allowsMembershipChanges() && (invoker.canUndo() || invoker.canRedo())) {
            invoker = new CommandInvoker();
            feed.note("Move history cleared: " + team.name() + " is " + team.state().label());
        }
    }

    // -- Read model ------------------------------------------------------------
    public synchronized List<Team> teams() {
        return teams;
    }

    public synchronized String strategy() {
        return strategy;
    }

    public synchronized List<Explanation> explanations() {
        return explanations;
    }

    /** The score of the board as it is now, after any manual moves. */
    public synchronized Optional<Score> score() {
        return round == null ? Optional.empty() : Optional.of(scorer.score(round, teams));
    }

    /** The constraints the board breaks as it is now. */
    public synchronized List<String> violations() {
        List<String> broken = new ArrayList<>();
        if (round != null) {
            Assignment assignment = new Assignment(teams);
            for (Constraint c : round.constraints()) {
                if (!c.isSatisfiedBy(assignment)) {
                    broken.add(c.describe());
                }
            }
        }
        return broken;
    }

    public synchronized List<ActivityFeed.Entry> activity() {
        return feed.entries();
    }

    private Team team(String name) {
        return teams.stream().filter(t -> t.name().equals(name)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("no team named " + name));
    }

    private static long idOf(Student s) {
        return s.getId() == null ? -1 : s.getId();
    }
}
