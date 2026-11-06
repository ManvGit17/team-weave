package io.teamweave.web;

import io.teamweave.application.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/*
 * Web layer: renders the student roster and handles registrations.
 */
@Controller
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("studentForm", new StudentForm());
        return "index";
    }

    @PostMapping("/students")
    public String register(@Valid @ModelAttribute("studentForm") StudentForm form,
                           BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("students", studentService.getAllStudents());
            return "index";
        }
        try {
            studentService.register(form.getName(), form.getEmail());
        } catch (IllegalArgumentException ex) {
            binding.rejectValue("email", "duplicate", ex.getMessage());
            model.addAttribute("students", studentService.getAllStudents());
            return "index";
        }
        return "redirect:/";
    }
}
