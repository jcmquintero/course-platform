package com.courses.platform.instructor.dto;

import java.util.UUID;

public record InstructorResponse(
        UUID id,
        String name,
        String email,
        String bio
) {
}