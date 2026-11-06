package io.teamweave.application;

import io.teamweave.domain.student.Student;
import io.teamweave.infrastructure.StudentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for managing students. */
@Service
@Transactional
public class StudentService {

    private final StudentRepository students;

    public StudentService(StudentRepository students) {
        this.students = students;
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return students.findAll();
    }

    /** Register a new student by name and email; emails must be unique. */
    public Student register(String name, String email) {
        if (students.existsByEmail(email)) {
            throw new IllegalArgumentException("a student with that email already exists");
        }
        return students.save(new Student(name, email));
    }

    public Student save(Student student) {
        return students.save(student);
    }

    @Transactional(readOnly = true)
    public long count() {
        return students.count();
    }
}
