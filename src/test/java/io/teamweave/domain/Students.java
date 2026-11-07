package io.teamweave.domain;

import io.teamweave.domain.student.Role;
import io.teamweave.domain.student.Student;
import org.springframework.test.util.ReflectionTestUtils;

/** Builds in-memory students (with ids) for the domain tests. */
public final class Students {

    private Students() {
    }

    public static Student student(long id, String name) {
        Student s = new Student(name, name.toLowerCase().replace(" ", "") + "@ex.edu");
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }

    public static Student skilled(long id, String name, String skill, int level) {
        Student s = student(id, name);
        s.addSkill(skill, level);
        return s;
    }

    public static Student wanting(long id, String name, Role role, String skill, int level) {
        Student s = skilled(id, name, skill, level);
        s.getDesiredRoles().add(role);
        return s;
    }
}
