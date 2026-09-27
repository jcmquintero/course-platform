package com.courses.platform.enrollment.dto;

import java.time.Instant;
import java.util.UUID;

import com.courses.platform.enrollment.EnrollmentStatus;

public record EnrollmentResponse(
        UUID id,
        UUID studentId,
        UUID courseId,
        EnrollmentStatus status,
        int progress,
        Instant enrolledAt) {
}