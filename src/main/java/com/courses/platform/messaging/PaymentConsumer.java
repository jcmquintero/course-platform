package com.courses.platform.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.courses.platform.messaging.event.EnrollmentCreatedEvent;
import com.courses.platform.payment.PaymentService;

import tools.jackson.databind.ObjectMapper;

@Component
public class PaymentConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    public PaymentConsumer(
            ObjectMapper objectMapper,
            PaymentService paymentService) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    @RabbitListener(queues = RabbitMqConfig.PAYMENT_QUEUE)
    public void consume(String payload) {
        EnrollmentCreatedEvent event =
                objectMapper.readValue(
                        payload,
                        EnrollmentCreatedEvent.class);

        paymentService.processEnrollmentCreated(event);
    }
}