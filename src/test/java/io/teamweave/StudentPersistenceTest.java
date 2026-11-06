package io.teamweave;

import static org.assertj.core.api.Assertions.assertThat;

import io.teamweave.domain.student.Availability;
import io.teamweave.domain.student.Role;
import io.teamweave.domain.student.Student;
import io.teamweave.infrastructure.StudentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** The rich student profile round-trips through PostgreSQL intact. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudentPersistenceTest {

    @Autowired
    private StudentRepository students;

    @PersistenceContext
    private EntityManager em;

    @Test
    void persistsAndReloadsFullProfile() {
        Student student = new Student("Ria Das", "ria@example.edu");
        student.addSkill("java", 4).addSkill("sql", 2);
        student.getAvailability().add(Availability.MON_AM);
        student.getInterests().add("backend");
        student.getDesiredRoles().add(Role.BACKEND);

        Long id = students.save(student).getId();
        em.flush();
        em.clear(); // force a real reload from the database

        Student loaded = students.findById(id).orElseThrow();
        assertThat(loaded.getEmail()).isEqualTo("ria@example.edu");
        assertThat(loaded.levelOf("java")).isEqualTo(4);
        assertThat(loaded.getSkills()).hasSize(2);
        assertThat(loaded.getAvailability()).contains(Availability.MON_AM);
        assertThat(loaded.getInterests()).contains("backend");
        assertThat(loaded.getDesiredRoles()).contains(Role.BACKEND);
    }
}
