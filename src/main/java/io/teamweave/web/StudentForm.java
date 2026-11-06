package io.teamweave.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Form-backing object for registering a student. */
public class StudentForm {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
