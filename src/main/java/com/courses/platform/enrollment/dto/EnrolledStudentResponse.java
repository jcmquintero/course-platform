package com.courses.platform.enrollment.dto;

import java.util.UUID;

public record EnrolledStudentResponse(
        UUID id,
        String firstName,
        String lastName,
        String email) {
}