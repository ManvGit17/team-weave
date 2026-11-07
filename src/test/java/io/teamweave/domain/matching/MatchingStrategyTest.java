package io.teamweave.domain.matching;

import static org.assertj.core.api.Assertions.assertThat;

import io.teamweave.domain.Students;
import io.teamweave.domain.constraint.MaxTeamSize;
import io.teamweave.domain.formation.MatchResult;
import io.teamweave.domain.formation.Round;
import io.teamweave.domain.student.Role;
import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.Team;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

class MatchingStrategyTest {

    static Stream<MatchingStrategy> strategies() {
        return Stream.of(new GreedyBalancerStrategy(), new PreferenceFirstStrategy(),
                         new SkillCoverageStrategy());
    }

    private static List<Student> cohort() {
        return List.of(
            Students.wanting(1, "Ava", Role.BACKEND, "java", 5),
            Students.wanting(2, "Ben", Role.FRONTEND, "react", 5),
            Students.wanting(3, "Cara", Role.DATA, "python", 4),
            Students.wanting(4, "Dev", Role.BACKEND, "java", 2),
            Students.wanting(5, "Eli", Role.DESIGN, "design", 4),
            Students.wanting(6, "Fay", Role.PRODUCT, "product", 3),
            Students.wanting(7, "Gus", Role.FRONTEND, "react", 2),
            Students.wanting(8, "Hana", Role.QA, "sql", 3));
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void placesEveryStudentExactlyOnce(MatchingStrategy strategy) {
        MatchResult result = strategy.assign(new Round(cohort(), 4));

        assertThat(result.teams()).hasSize(2);
        List<Long> placed = result.teams().stream()
            .flatMap(t -> t.students().stream()).map(Student::getId).toList();
        assertThat(placed).hasSize(8).doesNotHaveDuplicates();
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void scoresAndExplainsTheResult(MatchingStrategy strategy) {
        MatchResult result = strategy.assign(new Round(cohort(), 4));

        assertThat(result.strategy()).isEqualTo(strategy.name());
        assertThat(result.explanations()).hasSize(8);
        assertThat(result.score().overall()).isBetween(0.0, 1.0);
        assertThat(result.score().skillCoverage()).isBetween(0.0, 1.0);
        assertThat(result.score().balance()).isBetween(0.0, 1.0);
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void reportsViolatedConstraints(MatchingStrategy strategy) {
        Round round = new Round(cohort(), 4, List.of(new MaxTeamSize(3)));
        MatchResult result = strategy.assign(round);

        assertThat(result.satisfiesAllConstraints()).isFalse();
        assertThat(result.violations()).containsExactly("team size at most 3");
    }

    @Test
    void preferenceFirstPutsMutualPeersTogether() {
        List<Student> students = cohort();
        students.get(0).getPreferredPeerIds().add(3L); // Ava wants Cara
        students.get(2).getPreferredPeerIds().add(1L); // Cara wants Ava

        MatchResult result = new PreferenceFirstStrategy().assign(new Round(students, 4));

        Team avaTeam = result.teams().stream().filter(t -> t.contains(students.get(0)))
            .findFirst().orElseThrow();
        assertThat(avaTeam.contains(students.get(2))).isTrue();
        assertThat(result.score().preferenceSatisfaction()).isEqualTo(1.0);
    }

    @Test
    void preferenceFirstHonoursAOneWayPreferenceFromAnEarlierStudent() {
        List<Student> students = cohort();
        students.get(0).getPreferredPeerIds().add(3L); // Ava (placed first) wants Cara
        students.get(1).getPreferredPeerIds().add(7L); // Ben wants Gus

        MatchResult result = new PreferenceFirstStrategy().assign(new Round(students, 4));

        assertThat(result.score().preferenceSatisfaction()).isEqualTo(1.0);
    }

    @Test
    void greedyBalancerSpreadsStrengthEvenly() {
        MatchResult result = new GreedyBalancerStrategy().assign(new Round(cohort(), 4));
        // strengths 5,5,4,4,3,3,2,2 dealt in a snake: both teams total 14
        assertThat(result.teams()).allSatisfy(
            t -> assertThat(t.skillProfile().totalStrength()).isPositive());
        assertThat(result.score().balance()).isGreaterThan(0.8);
    }

    @Test
    void skillCoverageGivesEachTeamBroadSkills() {
        MatchResult result = new SkillCoverageStrategy().assign(new Round(cohort(), 4));
        assertThat(result.teams()).allSatisfy(
            t -> assertThat(t.skillProfile().bestLevels().size()).isGreaterThanOrEqualTo(3));
    }
}
