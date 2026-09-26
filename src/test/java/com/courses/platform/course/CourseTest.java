package com.courses.platform.course;

import com.courses.platform.category.Category;
import com.courses.platform.instructor.Instructor;
import com.courses.platform.shared.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CourseTest {

    @Test
    void newCourseStartsAsDraftWithNoOccupiedSeats() {
        Course course = createCourse();

        assertEquals(CourseStatus.DRAFT, course.getStatus());
        assertEquals(0, course.getOccupiedSeats());
    }

    @Test
    void draftCourseCanBePublished() {
        Course course = createCourse();

        course.publish();

        assertEquals(CourseStatus.PUBLISHED, course.getStatus());
    }

    @Test
    void publishedCourseCannotBePublishedAgain() {
        Course course = createCourse();
        course.publish();

        assertThrows(
                InvalidStateTransitionException.class,
                course::publish
        );
    }

    @Test
    void publishedCourseCanBeArchived() {
        Course course = createCourse();
        course.publish();

        course.archive();

        assertEquals(CourseStatus.ARCHIVED, course.getStatus());
    }

    @Test
    void archivedCourseCannotBeArchivedAgain() {
        Course course = createCourse();
        course.archive();

        assertThrows(
                InvalidStateTransitionException.class,
                course::archive
        );
    }

    private Course createCourse() {
        Category category = new Category(
                "Backend",
                "Backend development"
        );

        Instructor instructor = new Instructor(
                "Jane Doe",
                "jane@example.com",
                "Backend instructor"
        );

        return new Course(
                "Spring Boot",
                "Spring Boot course",
                20,
                CourseLevel.INTERMEDIATE,
                new BigDecimal("99.90"),
                10,
                category,
                instructor
        );
    }
}