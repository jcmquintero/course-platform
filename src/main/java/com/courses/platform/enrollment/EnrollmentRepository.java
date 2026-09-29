package com.courses.platform.enrollment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.courses.platform.course.Course;
import com.courses.platform.enrollment.dto.EnrolledCourseResponse;
import com.courses.platform.enrollment.dto.EnrolledStudentResponse;
import com.courses.platform.student.Student;

import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    boolean existsByStudentIdAndCourseIdAndStatusNot(
            UUID studentId,
            UUID courseId,
            EnrollmentStatus status);

    Optional<Enrollment> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            select new com.courses.platform.enrollment.dto.EnrolledStudentResponse(
                s.id,
                s.firstName,
                s.lastName,
                s.email
            )
            from Enrollment e
            join e.student s
            where e.course.id = :courseId
              and e.status <> com.courses.platform.enrollment.EnrollmentStatus.CANCELLED
            """)
    Page<EnrolledStudentResponse> findStudentsByCourseId(
            @Param("courseId") UUID courseId,
            Pageable pageable);

    @Query("""
            select new com.courses.platform.enrollment.dto.EnrolledCourseResponse(
                c.id,
                c.title,
                c.level,
                c.status
            )
            from Enrollment e
            join e.course c
            where e.student.id = :studentId
              and e.status <> com.courses.platform.enrollment.EnrollmentStatus.CANCELLED
            """)
    Page<EnrolledCourseResponse> findCoursesByStudentId(
            @Param("studentId") UUID studentId,
            Pageable pageable);

}