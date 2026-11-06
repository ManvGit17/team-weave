package com.example.mvc.service;

import org.springframework.stereotype.Service;
import java.util.List;

import com.example.mvc.model.Student;
import com.example.mvc.repository.StudentRepository;

/*
 * Service Layer
 * Contains business logic and interacts with repository
 */

@Service
public class StudentService {

    private final StudentRepository studentRepo;

    // Constructor Injection
    public StudentService(StudentRepository studentRepo) {
        this.studentRepo = studentRepo;
    }

    // Fetch all students
    public List<Student> getAllStudents() {
        return studentRepo.findAll();
    }

    // Save student to database
    public void saveStudent(Student studentObj) {
        studentRepo.save(studentObj);
    }
}