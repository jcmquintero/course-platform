package com.courses.platform.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EnrollmentCreatedEvent(
        UUID eventId,
        int version,
        Instant occurredAt,
        UUID enrollmentId,
        UUID studentId,
        UUID courseId,
        UUID paymentId,
        BigDecimal amount,
        String currency) {
}