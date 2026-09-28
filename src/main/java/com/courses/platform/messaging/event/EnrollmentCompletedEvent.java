package com.courses.platform.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record EnrollmentCompletedEvent(
        UUID eventId,
        int version,
        Instant occurredAt,
        UUID enrollmentId,
        UUID studentId,
        UUID courseId) {
}