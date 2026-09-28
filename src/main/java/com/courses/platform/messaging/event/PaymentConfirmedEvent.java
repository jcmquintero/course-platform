package com.courses.platform.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentConfirmedEvent(
        UUID eventId,
        int version,
        Instant occurredAt,
        UUID paymentId,
        UUID enrollmentId) {
}