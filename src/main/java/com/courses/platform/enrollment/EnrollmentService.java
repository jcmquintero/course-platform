package com.courses.platform.enrollment;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courses.platform.course.Course;
import com.courses.platform.course.CourseRepository;
import com.courses.platform.course.CourseStatus;
import com.courses.platform.enrollment.dto.CreateEnrollmentRequest;
import com.courses.platform.enrollment.dto.EnrollmentResponse;
import com.courses.platform.payment.Payment;
import com.courses.platform.payment.PaymentRepository;
import com.courses.platform.shared.ConflictException;
import com.courses.platform.shared.ResourceNotFoundException;
import com.courses.platform.student.Student;
import com.courses.platform.student.StudentRepository;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final PaymentRepository paymentRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            PaymentRepository paymentRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public EnrollmentResponse enroll(
            CreateEnrollmentRequest request,
            String idempotencyKey) {

        Enrollment existingEnrollment = enrollmentRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElse(null);

        if (existingEnrollment != null) {
            boolean sameRequest = existingEnrollment.getStudent().getId().equals(request.studentId())
                    && existingEnrollment.getCourse().getId().equals(request.courseId());

            if (!sameRequest) {
                throw new ConflictException(
                        "Idempotency-Key was already used for a different enrollment");
            }

            return toResponse(existingEnrollment);
        }

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (enrollmentRepository.existsByStudentIdAndCourseIdAndStatusNot(
                student.getId(),
                course.getId(),
                EnrollmentStatus.CANCELLED)) {
            throw new ConflictException("Student is already enrolled in this course");
        }

        int updatedRows = courseRepository.reserveSeat(
                course.getId(),
                CourseStatus.PUBLISHED);

        if (updatedRows == 0) {
            throw new ConflictException("Course is not available for enrollment");
        }

        Enrollment enrollment = new Enrollment(student, course, idempotencyKey);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        Payment payment = new Payment(
                savedEnrollment,
                course.getPrice(),
                "EUR",
                idempotencyKey);

        paymentRepository.save(payment);

        return toResponse(savedEnrollment);
    }

    @Transactional
    public EnrollmentResponse cancel(UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        enrollment.cancel();

        int updatedRows = courseRepository.releaseSeat(
                enrollment.getCourse().getId());

        if (updatedRows == 0) {
            throw new ConflictException("Could not release course seat");
        }

        return toResponse(enrollment);
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getCourse().getId(),
                enrollment.getStatus(),
                enrollment.getProgress(),
                enrollment.getEnrolledAt());
    }
}