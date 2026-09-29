package com.courses.platform.course.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.courses.platform.course.CourseLevel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCourseRequest(
        @NotBlank String title,
        String description,
        @Min(1) int estimatedHours,
        @NotNull CourseLevel level,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Min(1) int maxSeats,
        @NotNull UUID categoryId,
        @NotNull UUID instructorId) {
}