package com.courses.platform.enrollment;

import com.courses.platform.course.Course;
import com.courses.platform.shared.InvalidStateTransitionException;
import com.courses.platform.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @Column(nullable = false)
    private int progress;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    protected Enrollment() {
    }

    public Enrollment(Student student, Course course, String idempotencyKey) {
        this.id = UUID.randomUUID();
        this.student = student;
        this.course = course;
        this.status = EnrollmentStatus.PENDING_PAYMENT;
        this.progress = 0;
        this.enrolledAt = Instant.now();
        this.idempotencyKey = idempotencyKey;
    }

    public void cancel() {
        if (status == EnrollmentStatus.CANCELLED) {
            throw new InvalidStateTransitionException(
                    "Enrollment is already cancelled");
        }

        if (status == EnrollmentStatus.COMPLETED) {
            throw new InvalidStateTransitionException(
                    "Completed enrollment cannot be cancelled");
        }

        status = EnrollmentStatus.CANCELLED;
        cancelledAt = Instant.now();
    }

    public void activate() {
        if (status == EnrollmentStatus.ACTIVE) {
            throw new InvalidStateTransitionException(
                    "Enrollment is already active");
        }

        if (status != EnrollmentStatus.PENDING_PAYMENT) {
            throw new InvalidStateTransitionException(
                    "Only pending enrollment can be activated");
        }

        status = EnrollmentStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}