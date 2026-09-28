package com.courses.platform.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.courses.platform.enrollment.EnrollmentService;
import com.courses.platform.messaging.event.PaymentConfirmedEvent;

import tools.jackson.databind.ObjectMapper;

@Component
public class EnrollmentConsumer {

    private final ObjectMapper objectMapper;
    private final EnrollmentService enrollmentService;

    public EnrollmentConsumer(
            ObjectMapper objectMapper,
            EnrollmentService enrollmentService) {
        this.objectMapper = objectMapper;
        this.enrollmentService = enrollmentService;
    }

    @RabbitListener(queues = RabbitMqConfig.ENROLLMENT_QUEUE)
    public void consume(String payload) {
        PaymentConfirmedEvent event =
                objectMapper.readValue(
                        payload,
                        PaymentConfirmedEvent.class);

        enrollmentService.processPaymentConfirmed(event);
    }
}