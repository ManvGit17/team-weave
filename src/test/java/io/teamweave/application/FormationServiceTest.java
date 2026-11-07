package io.teamweave.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.teamweave.domain.formation.MatchResult;
import io.teamweave.domain.student.Role;
import io.teamweave.domain.student.Student;
import io.teamweave.infrastructure.StudentRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** The strategies run end to end over students loaded from PostgreSQL. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FormationServiceTest {

    @Autowired
    private FormationService formation;

    @Autowired
    private StudentRepository students;

    @BeforeEach
    void cohort() {
        students.deleteAll();
        String[][] people = {
            {"Ava", "java"}, {"Ben", "react"}, {"Cara", "python"}, {"Dev", "java"},
            {"Eli", "design"}, {"Fay", "product"}, {"Gus", "react"}, {"Hana", "sql"}};
        Role[] roles = Role.values();
        for (int i = 0; i < people.length; i++) {
            Student s = new Student(people[i][0], people[i][0].toLowerCase() + "@svc.test");
            s.addSkill(people[i][1], 2 + (i % 4));
            s.getDesiredRoles().add(roles[i % roles.length]);
            students.save(s);
        }
    }

    @Test
    void listsTheThreeStrategies() {
        assertThat(formation.strategyNames())
            .containsExactly("greedy-balancer", "preference-first", "skill-coverage");
    }

    @Test
    void formsTeamsWithTheChosenStrategy() {
        MatchResult result = formation.form(4, "skill-coverage", List.of());
        assertThat(result.strategy()).isEqualTo("skill-coverage");
        assertThat(result.teams()).hasSize(2);
        assertThat(result.teams().stream().mapToInt(t -> t.size()).sum()).isEqualTo(8);
    }

    @Test
    void comparesEveryStrategyBestFirst() {
        List<MatchResult> results = formation.compare(4, List.of());
        assertThat(results).hasSize(3);
        assertThat(results).extracting(r -> r.score().overall())
            .isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Test
    void unknownStrategyFallsBackToTheBalancer() {
        assertThat(formation.form(4, "nope", List.of()).strategy()).isEqualTo("greedy-balancer");
    }
}
