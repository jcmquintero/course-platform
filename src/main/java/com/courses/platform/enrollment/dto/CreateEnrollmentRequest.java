package com.courses.platform.enrollment.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateEnrollmentRequest(
        @NotNull UUID studentId,
        @NotNull UUID courseId) {
}