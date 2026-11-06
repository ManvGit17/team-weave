package io.teamweave.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import io.teamweave.application.StudentService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StudentService studentService;

    @Test
    void homeRendersRosterAndForm() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of());

        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("students", "studentForm"));
    }

    @Test
    void validRegistrationRedirectsHome() throws Exception {
        mvc.perform(post("/students")
                .param("name", "Ria Das")
                .param("email", "ria@example.edu"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        verify(studentService).register("Ria Das", "ria@example.edu");
    }

    @Test
    void invalidRegistrationRedisplaysFormWithErrors() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of());

        mvc.perform(post("/students")
                .param("name", "")
                .param("email", "not-an-email"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"))
            .andExpect(model().attributeHasFieldErrors("studentForm", "name", "email"));
    }
}
