package com.courses.platform.enrollment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.courses.platform.category.Category;
import com.courses.platform.category.CategoryRepository;
import com.courses.platform.course.Course;
import com.courses.platform.course.CourseLevel;
import com.courses.platform.course.CourseRepository;
import com.courses.platform.enrollment.dto.CreateEnrollmentRequest;
import com.courses.platform.instructor.Instructor;
import com.courses.platform.instructor.InstructorRepository;
import com.courses.platform.shared.ConflictException;
import com.courses.platform.student.Student;
import com.courses.platform.student.StudentRepository;

@SpringBootTest
@Testcontainers
class EnrollmentConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

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

    private Authentication adminAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void shouldNotExceedCourseCapacityWhenTwoStudentsEnrollConcurrently() throws Exception {
        Category category = categoryRepository.save(
                new Category("Backend", "Backend courses"));

        Instructor instructor = instructorRepository.save(
                new Instructor(
                        "John Doe",
                        "john@test.com",
                        "Backend instructor"));

        Course course = new Course(
                "Spring Boot",
                "Spring Boot course",
                20,
                CourseLevel.INTERMEDIATE,
                new BigDecimal("49.90"),
                1,
                category,
                instructor);

        course.publish();
        courseRepository.save(course);

        Student firstStudent = studentRepository.save(
                new Student(
                        "Alice",
                        "Smith",
                        "alice@test.com"));

        Student secondStudent = studentRepository.save(
                new Student(
                        "Bob",
                        "Taylor",
                        "bob@test.com"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> firstEnrollment = () -> {
            start.await();

            try {
                enrollmentService.enroll(
                        new CreateEnrollmentRequest(
                                firstStudent.getId(),
                                course.getId()),
                        "concurrent-001",
                        adminAuthentication());

                return true;
            } catch (ConflictException exception) {
                return false;
            }
        };

        Callable<Boolean> secondEnrollment = () -> {
            start.await();

            try {
                enrollmentService.enroll(
                        new CreateEnrollmentRequest(
                                secondStudent.getId(),
                                course.getId()),
                        "concurrent-002",
                        adminAuthentication());

                return true;
            } catch (ConflictException exception) {
                return false;
            }
        };

        Future<Boolean> firstResult = executor.submit(firstEnrollment);
        Future<Boolean> secondResult = executor.submit(secondEnrollment);

        start.countDown();

        boolean firstSucceeded = firstResult.get();
        boolean secondSucceeded = secondResult.get();

        executor.shutdown();

        long successfulEnrollments = Stream
                .of(firstSucceeded, secondSucceeded)
                .filter(Boolean::booleanValue)
                .count();

        Course updatedCourse = courseRepository
                .findById(course.getId())
                .orElseThrow();

        assertEquals(1, successfulEnrollments);
        assertEquals(1, updatedCourse.getOccupiedSeats());
    }
}