package com.example.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.mvc.model.Student;
import com.example.mvc.service.StudentService;

/*
 * Controller Layer
 * Handles incoming requests and response mapping
 */

@Controller
@RequestMapping("/")
public class StudentController {

    private final StudentService studentService;

    // Constructor Injection
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Load Home Page with Student Data
    @GetMapping
    public String loadHomePage(Model viewModel) {

        viewModel.addAttribute("studentList", studentService.getAllStudents());
        viewModel.addAttribute("studentForm", new Student());

        return "index";
    }

    // Handle Form Submission
    @PostMapping("save")
    public String addStudent(@ModelAttribute("studentForm") Student studentObj) {

        studentService.saveStudent(studentObj);

        return "redirect:/";
    }
}