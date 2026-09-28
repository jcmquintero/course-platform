package com.courses.platform.outbox;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.courses.platform.messaging.RabbitMqConfig;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            RabbitTemplate rabbitTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        var events = outboxEventRepository
                .findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EVENTS_EXCHANGE,
                    routingKey(event),
                    event.getPayload());

            event.markAsPublished();
        }
    }

    private String routingKey(OutboxEvent event) {
        return switch (event.getEventType()) {
            case "EnrollmentCreated" ->
                RabbitMqConfig.ENROLLMENT_CREATED_ROUTING_KEY;
            case "PaymentConfirmed" ->
                RabbitMqConfig.PAYMENT_CONFIRMED_ROUTING_KEY;
            default ->
                throw new IllegalArgumentException(
                        "Unsupported event type: " + event.getEventType());
        };
    }
}