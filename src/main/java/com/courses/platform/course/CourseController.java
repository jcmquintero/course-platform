package com.courses.platform.course;

import com.courses.platform.course.dto.CourseResponse;
import com.courses.platform.course.dto.CreateCourseRequest;
import com.courses.platform.course.dto.UpdateCourseRequest;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CreateCourseRequest request) {
        return courseService.create(request);
    }

    @GetMapping("/{id}")
    public CourseResponse findById(@PathVariable UUID id) {
        return courseService.findById(id);
    }

    @PostMapping("/{id}/publish")
    public CourseResponse publish(@PathVariable UUID id, Authentication authentication) {
        return courseService.publish(id, authentication);
    }

    @PostMapping("/{id}/archive")
    public CourseResponse archive(@PathVariable UUID id, Authentication authentication) {
        return courseService.archive(id, authentication);
    }

    @GetMapping
    public Page<CourseResponse> search(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) CourseLevel level,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean available,
            Pageable pageable) {

        return courseService.search(
                categoryId,
                level,
                minPrice,
                maxPrice,
                title,
                available,
                pageable);
    }

    @PutMapping("/{id}")
    public CourseResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseRequest request,
            Authentication authentication) {

        return courseService.update(
                id,
                request,
                authentication);
    }
}