package com.courses.platform.enrollment;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courses.platform.course.Course;
import com.courses.platform.course.CourseRepository;
import com.courses.platform.course.CourseStatus;
import com.courses.platform.enrollment.dto.CreateEnrollmentRequest;
import com.courses.platform.enrollment.dto.EnrollmentResponse;
import com.courses.platform.messaging.ProcessedEvent;
import com.courses.platform.messaging.ProcessedEventRepository;
import com.courses.platform.messaging.event.EnrollmentCompletedEvent;
import com.courses.platform.messaging.event.EnrollmentCreatedEvent;
import com.courses.platform.messaging.event.PaymentConfirmedEvent;
import com.courses.platform.outbox.OutboxEvent;
import com.courses.platform.outbox.OutboxEventRepository;
import com.courses.platform.payment.Payment;
import com.courses.platform.payment.PaymentRepository;
import com.courses.platform.shared.ConflictException;
import com.courses.platform.shared.ResourceNotFoundException;
import com.courses.platform.student.Student;
import com.courses.platform.student.StudentRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;
    private final Counter enrollmentsCreatedCounter;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            PaymentRepository paymentRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            ProcessedEventRepository processedEventRepository,
            MeterRegistry meterRegistry) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.paymentRepository = paymentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.processedEventRepository = processedEventRepository;
        this.enrollmentsCreatedCounter = meterRegistry.counter("enrollments.created");
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

        UUID eventId = UUID.randomUUID();

        EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(
                eventId,
                1,
                java.time.Instant.now(),
                savedEnrollment.getId(),
                student.getId(),
                course.getId(),
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency());

        try {
            String payload = objectMapper.writeValueAsString(event);

            outboxEventRepository.save(
                    new OutboxEvent(
                            eventId,
                            "EnrollmentCreated",
                            savedEnrollment.getId(),
                            payload));
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Could not serialize EnrollmentCreated event",
                    exception);
        }

        enrollmentsCreatedCounter.increment();
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

    @Transactional
    public void processPaymentConfirmed(PaymentConfirmedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        Enrollment enrollment = enrollmentRepository
                .findById(event.enrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found"));

        enrollment.activate();

        processedEventRepository.save(
                new ProcessedEvent(event.eventId()));
    }

    @Transactional
    public EnrollmentResponse updateProgress(
            UUID enrollmentId,
            UpdateProgressRequest request) {

        Enrollment enrollment = enrollmentRepository
                .findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found"));

        boolean completed = enrollment.updateProgress(request.progress());

        if (completed) {
            UUID eventId = UUID.randomUUID();

            EnrollmentCompletedEvent event = new EnrollmentCompletedEvent(
                    eventId,
                    1,
                    Instant.now(),
                    enrollment.getId(),
                    enrollment.getStudent().getId(),
                    enrollment.getCourse().getId());

            String payload = objectMapper.writeValueAsString(event);

            outboxEventRepository.save(
                    new OutboxEvent(
                            eventId,
                            "EnrollmentCompleted",
                            enrollment.getId(),
                            payload));
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