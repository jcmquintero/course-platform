package com.courses.platform.enrollment;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.courses.platform.enrollment.dto.CreateEnrollmentRequest;
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
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return enrollmentService.enroll(request, idempotencyKey);
    }

    @PostMapping("/{id}/cancel")
    public EnrollmentResponse cancel(@PathVariable UUID id) {
        return enrollmentService.cancel(id);
    }

    @PatchMapping("/{id}/progress")
    public EnrollmentResponse updateProgress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProgressRequest request) {
        return enrollmentService.updateProgress(id, request);
    }
}