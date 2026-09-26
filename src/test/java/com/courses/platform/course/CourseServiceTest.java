package com.courses.platform.course;

import com.courses.platform.category.Category;
import com.courses.platform.category.CategoryRepository;
import com.courses.platform.course.dto.CourseResponse;
import com.courses.platform.course.dto.CreateCourseRequest;
import com.courses.platform.instructor.Instructor;
import com.courses.platform.instructor.InstructorRepository;
import com.courses.platform.shared.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CourseServiceTest {

    private CourseRepository courseRepository;
    private CategoryRepository categoryRepository;
    private InstructorRepository instructorRepository;
    private CourseService courseService;

    @BeforeEach
    void setUp() {
        courseRepository = Mockito.mock(CourseRepository.class);
        categoryRepository = Mockito.mock(CategoryRepository.class);
        instructorRepository = Mockito.mock(InstructorRepository.class);

        courseService = new CourseService(
                courseRepository,
                categoryRepository,
                instructorRepository
        );
    }

    @Test
    void createsCourseWhenCategoryAndInstructorExist() {
        Category category = new Category("Backend", "Backend courses");
        Instructor instructor = new Instructor(
                "Jane Doe",
                "jane@example.com",
                "Backend instructor"
        );

        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));

        when(instructorRepository.findById(instructor.getId()))
                .thenReturn(Optional.of(instructor));

        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateCourseRequest request = new CreateCourseRequest(
                "Spring Boot",
                "Spring Boot course",
                20,
                CourseLevel.INTERMEDIATE,
                new BigDecimal("99.90"),
                10,
                category.getId(),
                instructor.getId()
        );

        CourseResponse response = courseService.create(request);

        assertEquals("Spring Boot", response.title());
        assertEquals(CourseStatus.DRAFT, response.status());
        assertEquals(0, response.occupiedSeats());
        assertEquals(category.getId(), response.categoryId());
        assertEquals(instructor.getId(), response.instructorId());
    }

    @Test
    void failsWhenCreatingCourseWithUnknownCategory() {
        UUID categoryId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        CreateCourseRequest request = new CreateCourseRequest(
                "Spring Boot",
                "Spring Boot course",
                20,
                CourseLevel.INTERMEDIATE,
                new BigDecimal("99.90"),
                10,
                categoryId,
                instructorId
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> courseService.create(request)
        );
    }
}