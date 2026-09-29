package com.courses.platform.payment;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courses.platform.messaging.ProcessedEvent;
import com.courses.platform.messaging.ProcessedEventRepository;
import com.courses.platform.messaging.event.EnrollmentCreatedEvent;
import com.courses.platform.messaging.event.PaymentConfirmedEvent;
import com.courses.platform.outbox.OutboxEvent;
import com.courses.platform.outbox.OutboxEventRepository;
import com.courses.platform.shared.ResourceNotFoundException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import tools.jackson.databind.ObjectMapper;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Counter paymentsConfirmedCounter;

    public PaymentService(
            PaymentRepository paymentRepository,
            ProcessedEventRepository processedEventRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry) {
        this.paymentRepository = paymentRepository;
        this.processedEventRepository = processedEventRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.paymentsConfirmedCounter = meterRegistry.counter("payments.confirmed");
    }

    @Transactional
    public void processEnrollmentCreated(EnrollmentCreatedEvent event) {
        UUID eventId = event.eventId();

        if (processedEventRepository.existsById(eventId)) {
            return;
        }

        Payment payment = paymentRepository.findById(event.paymentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found"));

        payment.confirm();
        paymentsConfirmedCounter.increment();

        UUID paymentConfirmedEventId = UUID.randomUUID();

        PaymentConfirmedEvent paymentConfirmedEvent = new PaymentConfirmedEvent(
                paymentConfirmedEventId,
                1,
                Instant.now(),
                payment.getId(),
                event.enrollmentId());

        String payload = objectMapper.writeValueAsString(
                paymentConfirmedEvent);

        outboxEventRepository.save(
                new OutboxEvent(
                        paymentConfirmedEventId,
                        "PaymentConfirmed",
                        event.enrollmentId(),
                        payload));

        processedEventRepository.save(
                new ProcessedEvent(eventId));
    }
}