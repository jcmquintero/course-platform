package com.courses.platform.course;

import com.courses.platform.course.dto.CourseResponse;

public final class CourseMapper {

    private CourseMapper() {
    }

    public static CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getEstimatedHours(),
                course.getLevel(),
                course.getPrice(),
                course.getMaxSeats(),
                course.getOccupiedSeats(),
                course.getStatus(),
                course.getCategory().getId(),
                course.getInstructor().getId()
        );
    }
}