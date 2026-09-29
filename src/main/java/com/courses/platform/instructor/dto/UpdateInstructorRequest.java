package com.courses.platform.instructor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateInstructorRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String bio) {
}