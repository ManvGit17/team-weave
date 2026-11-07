package io.teamweave.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.teamweave.domain.Students;
import io.teamweave.domain.constraint.KeepTogether;
import io.teamweave.domain.formation.Round;
import io.teamweave.domain.matching.GreedyBalancerStrategy;
import io.teamweave.domain.student.Role;
import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.IllegalTransitionException;
import io.teamweave.domain.team.Team;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FormationSessionTest {

    private final List<Student> cohort = List.of(
        Students.wanting(1, "Ava", Role.BACKEND, "java", 5),
        Students.wanting(2, "Ben", Role.FRONTEND, "react", 5),
        Students.wanting(3, "Cara", Role.DATA, "python", 4),
        Students.wanting(4, "Dev", Role.BACKEND, "java", 2));

    private FormationSession board;

    @BeforeEach
    void formTeams() {
        board = new FormationSession();
        Round round = new Round(cohort, 2, List.of(new KeepTogether(cohort.get(0), cohort.get(1))));
        board.start(round, new GreedyBalancerStrategy().assign(round));
    }

    private Team teamOf(long studentId) {
        return board.teams().stream()
            .filter(t -> t.students().stream().anyMatch(s -> s.getId() == studentId))
            .findFirst().orElseThrow();
    }

    private Team otherThan(Team team) {
        return board.teams().stream().filter(t -> t != team).findFirst().orElseThrow();
    }

    @Test
    void startsWithTheStrategysTeams() {
        assertThat(board.hasFormation()).isTrue();
        assertThat(board.strategy()).isEqualTo("greedy-balancer");
        assertThat(board.teams()).hasSize(2);
        assertThat(board.activity()).extracting(ActivityFeed.Entry::text)
            .contains("Formed 2 teams with greedy-balancer");
    }

    @Test
    void movesAreCommandsThatUndoAndRedo() {
        Team from = teamOf(1);
        Team to = otherThan(from);

        board.move(1, to.name());
        assertThat(teamOf(1)).isSameAs(to);
        assertThat(board.history()).hasSize(1);

        board.undo();
        assertThat(teamOf(1)).isSameAs(from);
        assertThat(board.canRedo()).isTrue();

        board.redo();
        assertThat(teamOf(1)).isSameAs(to);
    }

    @Test
    void theActivityFeedObservesEveryMove() {
        Team to = otherThan(teamOf(1));
        board.move(1, to.name());

        assertThat(board.activity()).extracting(ActivityFeed.Entry::text)
            .anySatisfy(t -> assertThat(t).contains("removed Ava"))
            .anySatisfy(t -> assertThat(t).contains(to.name() + ": added Ava"));
    }

    @Test
    void theScoreAndConstraintsFollowManualMoves() {
        // Ava and Ben are dealt to different teams; moving Ben next to Ava
        // satisfies the keep-together rule.
        assertThat(board.violations()).containsExactly("Ava with Ben");
        board.move(2, teamOf(1).name());
        assertThat(board.violations()).isEmpty();
        assertThat(board.score()).isPresent();
    }

    @Test
    void aLockedTeamCannotGainOrLoseMembers() {
        Team ava = teamOf(1);
        board.transition(ava.name(), "lock");

        assertThatThrownBy(() -> board.move(1, otherThan(ava).name()))
            .isInstanceOf(IllegalTransitionException.class)
            .hasMessageContaining("locked");
    }

    @Test
    void lockingClearsMoveHistoryThatCouldNoLongerBeUndone() {
        Team from = teamOf(1);
        board.move(1, otherThan(from).name());
        assertThat(board.canUndo()).isTrue();

        board.transition(from.name(), "lock");

        assertThat(board.canUndo()).isFalse();
        assertThat(board.activity()).extracting(ActivityFeed.Entry::text)
            .anySatisfy(t -> assertThat(t).contains("Move history cleared"));
    }

    @Test
    void lockingAlsoClearsAPendingRedo() {
        Team from = teamOf(1);
        board.move(1, otherThan(from).name());
        board.undo();
        assertThat(board.canRedo()).isTrue();

        board.transition(from.name(), "lock");

        assertThat(board.canRedo()).as("a redo out of a locked team could never apply").isFalse();
    }

    @Test
    void lifecycleRejectsAnInvalidStep() {
        assertThatThrownBy(() -> board.transition(teamOf(1).name(), "submit"))
            .isInstanceOf(IllegalTransitionException.class);
    }
}
