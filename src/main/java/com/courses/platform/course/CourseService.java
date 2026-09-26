package com.courses.platform.course;

import com.courses.platform.category.Category;
import com.courses.platform.category.CategoryRepository;
import com.courses.platform.course.dto.CourseResponse;
import com.courses.platform.course.dto.CreateCourseRequest;
import com.courses.platform.instructor.Instructor;
import com.courses.platform.instructor.InstructorRepository;
import com.courses.platform.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                        "Category not found: " + request.categoryId()
                ));

        Instructor instructor = instructorRepository.findById(request.instructorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instructor not found: " + request.instructorId()
                ));

        Course course = new Course(
                request.title(),
                request.description(),
                request.estimatedHours(),
                request.level(),
                request.price(),
                request.maxSeats(),
                category,
                instructor
        );

        Course savedCourse = courseRepository.save(course);

        return CourseMapper.toResponse(savedCourse);
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(UUID id) {
        Course course = getCourse(id);

        return CourseMapper.toResponse(course);
    }

    @Transactional
    public CourseResponse publish(UUID id) {
        Course course = getCourse(id);

        course.publish();

        return CourseMapper.toResponse(course);
    }

    @Transactional
    public CourseResponse archive(UUID id) {
        Course course = getCourse(id);

        course.archive();

        return CourseMapper.toResponse(course);
    }

    private Course getCourse(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found: " + id
                ));
    }
}