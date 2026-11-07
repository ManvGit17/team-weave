package io.teamweave.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.teamweave.domain.student.Role;
import io.teamweave.domain.student.Student;
import io.teamweave.infrastructure.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** The pages, the session-scoped board and the database, working together. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FormationWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private StudentRepository students;

    private final MockHttpSession session = new MockHttpSession();
    private final List<Long> ids = new ArrayList<>();

    @BeforeEach
    void cohort() {
        students.deleteAll();
        String[][] people = {{"Ava", "java"}, {"Ben", "react"}, {"Cara", "python"}, {"Dev", "sql"}};
        Role[] roles = {Role.BACKEND, Role.FRONTEND, Role.DATA, Role.QA};
        for (int i = 0; i < people.length; i++) {
            Student s = new Student(people[i][0], people[i][0].toLowerCase() + "@web.test");
            s.addSkill(people[i][1], 3 + i % 2);
            s.getDesiredRoles().add(roles[i]);
            ids.add(students.save(s).getId());
        }
    }

    private void formTeams() throws Exception {
        mvc.perform(post("/formation/run").session(session)
                .param("teamSize", "2").param("strategy", "greedy-balancer"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/formation"));
    }

    @Test
    void anEmptyBoardOffersTheSetupForm() throws Exception {
        mvc.perform(get("/formation").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Form teams")))
            .andExpect(content().string(containsString("preference-first")));
    }

    @Test
    void runningAStrategyFillsTheBoard() throws Exception {
        formTeams();
        mvc.perform(get("/formation").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Team 1")))
            .andExpect(content().string(containsString("Team 2")))
            .andExpect(content().string(containsString("Lock team")))
            .andExpect(content().string(containsString("Formed 2 teams with greedy-balancer")));
    }

    @Test
    void aMoveIsRecordedAndCanBeUndone() throws Exception {
        formTeams();
        // Strengths deal Ben, Dev, Ava, Cara in a snake: Ava lands in Team 2.
        mvc.perform(post("/formation/move").session(session)
            .param("studentId", String.valueOf(ids.get(0))).param("toTeam", "Team 1"));
        mvc.perform(get("/formation").session(session))
            .andExpect(content().string(containsString("move Ava from Team 2 to Team 1")));

        mvc.perform(post("/formation/undo").session(session));
        mvc.perform(get("/formation").session(session))
            .andExpect(content().string(containsString("No moves yet")));
    }

    @Test
    void aLockedTeamRefusesMovesWithAMessage() throws Exception {
        formTeams();
        mvc.perform(post("/formation/teams/{t}/lock", "Team 1").session(session))
            .andExpect(status().is3xxRedirection());
        mvc.perform(get("/formation").session(session))
            .andExpect(content().string(containsString("locked")));

        // Ava is in Team 2; Team 1 is locked.
        mvc.perform(post("/formation/move").session(session)
                .param("studentId", String.valueOf(ids.get(0))).param("toTeam", "Team 1"))
            .andExpect(flash().attribute("error",
                containsString("Cannot change the members of a team that is locked")));
    }

    @Test
    void undoThenLockThenRedoIsHandledGracefully() throws Exception {
        formTeams();
        // Ava moves from Team 2 into Team 1, is undone, then Team 2 is locked.
        mvc.perform(post("/formation/move").session(session)
            .param("studentId", String.valueOf(ids.get(0))).param("toTeam", "Team 1"));
        mvc.perform(post("/formation/undo").session(session));
        mvc.perform(post("/formation/teams/{t}/lock", "Team 2").session(session));

        mvc.perform(post("/formation/redo").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/formation"));
        mvc.perform(get("/formation").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Move history cleared")));
    }

    @Test
    void anOutOfRangeTeamSizeIsRejected() throws Exception {
        mvc.perform(post("/formation/run").session(session).param("teamSize", "1"))
            .andExpect(flash().attribute("error", "Team size must be between 2 and 12."));
    }

    @Test
    void theComparePageRanksEveryStrategy() throws Exception {
        mvc.perform(get("/compare").param("teamSize", "2"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("greedy-balancer")))
            .andExpect(content().string(containsString("preference-first")))
            .andExpect(content().string(containsString("skill-coverage")))
            .andExpect(content().string(containsString("best overall")));
    }

    @Test
    void howItsBuiltShowsThePatternsAndTheGeneratedDiagram() throws Exception {
        mvc.perform(get("/how-its-built"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Specification")))
            .andExpect(content().string(containsString("MoveStudentCommand")))
            .andExpect(content().string(containsString("classDiagram")))
            .andExpect(content().string(containsString("TeamState &lt;|.. Forming")))
            .andExpect(content().string(not(containsString("isn't packaged"))));
    }

    @Test
    void theRosterRendersSkillsAndRoles() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Ava")))
            .andExpect(content().string(containsString("BACKEND")));
    }
}
