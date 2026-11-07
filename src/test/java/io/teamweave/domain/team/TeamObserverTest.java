package io.teamweave.domain.team;

import static org.assertj.core.api.Assertions.assertThat;

import io.teamweave.domain.Students;
import io.teamweave.domain.student.Student;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TeamObserverTest {

    @Test
    void notifiesListenersOnMembershipAndStateChanges() {
        Team team = new Team("A");
        List<String> changes = new ArrayList<>();
        team.addListener((t, change) -> changes.add(change));

        Student ava = Students.student(1, "Ava");
        team.addMember(ava);
        team.removeMember(ava);
        team.lock();

        assertThat(changes).anySatisfy(c -> assertThat(c).contains("added Ava"));
        assertThat(changes).anySatisfy(c -> assertThat(c).contains("removed Ava"));
        assertThat(changes).contains("locked");
    }

    @Test
    void multipleListenersAllHear() {
        Team team = new Team("A");
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        team.addListener((t, c) -> a.add(c));
        team.addListener((t, c) -> b.add(c));

        team.addMember(Students.student(2, "Ben"));

        assertThat(a).hasSize(1);
        assertThat(b).hasSize(1);
    }
}
