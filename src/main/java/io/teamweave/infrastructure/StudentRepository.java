package io.teamweave.infrastructure;

import io.teamweave.domain.student.Student;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmail(String email);

    /**
     * Load every student with the collections the roster view needs, so they
     * render correctly without an open-in-view session.
     */
    @Override
    @EntityGraph(attributePaths = {"skills", "desiredRoles"})
    List<Student> findAll();
}
