package com.courses.platform.course;

import com.courses.platform.category.Category;
import com.courses.platform.category.CategoryRepository;
import com.courses.platform.course.dto.CourseResponse;
import com.courses.platform.course.dto.CreateCourseRequest;
import com.courses.platform.instructor.Instructor;
import com.courses.platform.instructor.InstructorRepository;
import com.courses.platform.shared.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final InstructorRepository instructorRepository;

    public CourseService(
            CourseRepository courseRepository,
            CategoryRepository categoryRepository,
            InstructorRepository instructorRepository) {

        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.instructorRepository = instructorRepository;
    }

    @Transactional
    public CourseResponse create(CreateCourseRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found: " + request.categoryId()));

        Instructor instructor = instructorRepository.findById(request.instructorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instructor not found: " + request.instructorId()));

        Course course = new Course(
                request.title(),
                request.description(),
                request.estimatedHours(),
                request.level(),
                request.price(),
                request.maxSeats(),
                category,
                instructor);

        Course savedCourse = courseRepository.save(course);

        return CourseMapper.toResponse(savedCourse);
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(UUID id) {
        Course course = getCourse(id);

        return CourseMapper.toResponse(course);
    }

    @Transactional
    public CourseResponse publish(UUID id, Authentication authentication) {
        Course course = getCourse(id);

        validateOwnership(course, authentication);

        course.publish();

        return CourseMapper.toResponse(course);
    }

    @Transactional
    public CourseResponse archive(UUID id, Authentication authentication) {
        Course course = getCourse(id);

        validateOwnership(course, authentication);

        course.archive();

        return CourseMapper.toResponse(course);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> search(
            UUID categoryId,
            CourseLevel level,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String title,
            Boolean available,
            Pageable pageable) {

        Specification<Course> specification = Specification.where(
                CourseSpecification.hasCategory(categoryId))
                .and(CourseSpecification.hasLevel(level))
                .and(CourseSpecification.priceAtLeast(minPrice))
                .and(CourseSpecification.priceAtMost(maxPrice))
                .and(CourseSpecification.titleContains(title))
                .and(CourseSpecification.hasAvailability(available));

        return courseRepository
                .findAll(specification, pageable)
                .map(this::toResponse);
    }

    private Course getCourse(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found: " + id));
    }

    private void validateOwnership(
            Course course,
            Authentication authentication) {

        boolean admin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (admin) {
            return;
        }

        String authenticatedEmail = authentication.getName();

        if (!course.getInstructor().getEmail()
                .equalsIgnoreCase(authenticatedEmail)) {
            throw new AccessDeniedException(
                    "You cannot modify another instructor's course");
        }
    }

    private CourseResponse toResponse(Course course) {
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
                course.getInstructor().getId());
    }
}