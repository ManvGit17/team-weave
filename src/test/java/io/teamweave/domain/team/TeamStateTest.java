package io.teamweave.domain.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TeamStateTest {

    @Test
    void walksTheHappyPath() {
        Team team = new Team("A");
        assertThat(team.state().label()).isEqualTo("forming");

        team.lock();
        assertThat(team.state().label()).isEqualTo("locked");
        team.activate();
        assertThat(team.state().label()).isEqualTo("active");
        team.submit();
        assertThat(team.state().label()).isEqualTo("submitted");
        team.review();
        assertThat(team.state().label()).isEqualTo("reviewed");
        assertThat(team.state().isTerminal()).isTrue();
    }

    @Test
    void rejectsAnInvalidTransition() {
        Team team = new Team("A"); // forming
        assertThatThrownBy(team::activate)
            .isInstanceOf(IllegalTransitionException.class)
            .hasMessageContaining("forming");
    }

    @Test
    void cannotTransitionOutOfATerminalState() {
        Team team = new Team("A");
        team.lock();
        team.activate();
        team.submit();
        team.review();
        assertThatThrownBy(team::lock).isInstanceOf(IllegalTransitionException.class);
    }
}
