package com.courses.platform.enrollment.dto;

import java.util.UUID;

import com.courses.platform.course.CourseLevel;
import com.courses.platform.course.CourseStatus;

public record EnrolledCourseResponse(
        UUID id,
        String title,
        CourseLevel level,
        CourseStatus status) {
}