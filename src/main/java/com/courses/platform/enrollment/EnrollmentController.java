package com.courses.platform.enrollment;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.courses.platform.enrollment.dto.CreateEnrollmentRequest;
import com.courses.platform.enrollment.dto.EnrolledCourseResponse;
import com.courses.platform.enrollment.dto.EnrolledStudentResponse;
import com.courses.platform.enrollment.dto.EnrollmentResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(
            @Valid @RequestBody CreateEnrollmentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {

        return enrollmentService.enroll(request, idempotencyKey, authentication);
    }

    @PostMapping("/{id}/cancel")
    public EnrollmentResponse cancel(@PathVariable UUID id, Authentication authentication) {
        return enrollmentService.cancel(id, authentication);
    }

    @PatchMapping("/{id}/progress")
    public EnrollmentResponse updateProgress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProgressRequest request,
            Authentication authentication) {
        return enrollmentService.updateProgress(id, request, authentication);
    }

    @GetMapping("/courses/{courseId}/students")
    public Page<EnrolledStudentResponse> findStudentsByCourse(
            @PathVariable UUID courseId,
            Pageable pageable) {

        return enrollmentService.findStudentsByCourse(
                courseId,
                pageable);
    }

    @GetMapping("/students/{studentId}/courses")
    public Page<EnrolledCourseResponse> findCoursesByStudent(
            @PathVariable UUID studentId,
            Pageable pageable) {

        return enrollmentService.findCoursesByStudent(
                studentId,
                pageable);
    }
}