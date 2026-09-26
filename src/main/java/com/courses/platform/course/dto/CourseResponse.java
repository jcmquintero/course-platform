package com.courses.platform.course.dto;

import com.courses.platform.course.CourseLevel;
import com.courses.platform.course.CourseStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String title,
        String description,
        int estimatedHours,
        CourseLevel level,
        BigDecimal price,
        int maxSeats,
        int occupiedSeats,
        CourseStatus status,
        UUID categoryId,
        UUID instructorId) {
}