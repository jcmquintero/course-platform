package com.courses.platform.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    boolean existsByStudentIdAndCourseIdAndStatusNot(
        UUID studentId,
        UUID courseId,
        EnrollmentStatus status);
    
    Optional<Enrollment> findByIdempotencyKey(String idempotencyKey);

}