package com.courses.platform.enrollment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.courses.platform.category.Category;
import com.courses.platform.category.CategoryRepository;
import com.courses.platform.course.Course;
import com.courses.platform.course.CourseLevel;
import com.courses.platform.course.CourseRepository;
import com.courses.platform.enrollment.dto.CreateEnrollmentRequest;
import com.courses.platform.enrollment.dto.EnrollmentResponse;
import com.courses.platform.instructor.Instructor;
import com.courses.platform.instructor.InstructorRepository;
import com.courses.platform.shared.InvalidStateTransitionException;
import com.courses.platform.student.Student;
import com.courses.platform.student.StudentRepository;

@SpringBootTest
@Testcontainers
class EnrollmentServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Test
    void shouldReleaseSeatOnlyOnceWhenEnrollmentIsCancelled() {
        Category category = categoryRepository.save(
                new Category("Backend", "Backend courses"));

        Instructor instructor = instructorRepository.save(
                new Instructor(
                        "Jane Doe",
                        "jane@test.com",
                        "Backend instructor"));

        Course course = new Course(
                "Java",
                "Java course",
                20,
                CourseLevel.BEGINNER,
                new BigDecimal("39.90"),
                1,
                category,
                instructor);

        course.publish();
        courseRepository.save(course);

        Student student = studentRepository.save(
                new Student(
                        "Alice",
                        "Smith",
                        "alice@test.com"));

        EnrollmentResponse enrollment = enrollmentService.enroll(
                new CreateEnrollmentRequest(
                        student.getId(),
                        course.getId()),
                "cancel-test-001");

        Course courseAfterEnrollment = courseRepository
                .findById(course.getId())
                .orElseThrow();

        assertEquals(1, courseAfterEnrollment.getOccupiedSeats());

        EnrollmentResponse cancelled = enrollmentService.cancel(enrollment.id());

        assertEquals(EnrollmentStatus.CANCELLED, cancelled.status());

        Course courseAfterCancellation = courseRepository
                .findById(course.getId())
                .orElseThrow();

        assertEquals(0, courseAfterCancellation.getOccupiedSeats());

        assertThrows(
                InvalidStateTransitionException.class,
                () -> enrollmentService.cancel(enrollment.id()));

        Course courseAfterSecondCancellation = courseRepository
                .findById(course.getId())
                .orElseThrow();

        assertEquals(0, courseAfterSecondCancellation.getOccupiedSeats());
    }
}