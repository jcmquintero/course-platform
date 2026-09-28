package com.courses.platform.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EVENTS_EXCHANGE = "course.events";

    public static final String PAYMENT_QUEUE = "payment.queue";

    public static final String ENROLLMENT_CREATED_ROUTING_KEY = "enrollment.created";

    public static final String PAYMENT_CONFIRMED_ROUTING_KEY = "payment.confirmed";

    public static final String ENROLLMENT_QUEUE = "enrollment.queue";

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE);
    }

    @Bean
    Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true);
    }

    @Bean
    Binding enrollmentCreatedBinding(
            Queue paymentQueue,
            TopicExchange eventsExchange) {

        return BindingBuilder
                .bind(paymentQueue)
                .to(eventsExchange)
                .with(ENROLLMENT_CREATED_ROUTING_KEY);
    }

    @Bean
    Queue enrollmentQueue() {
        return new Queue(ENROLLMENT_QUEUE, true);
    }

    @Bean
    Binding paymentConfirmedBinding(
            Queue enrollmentQueue,
            TopicExchange eventsExchange) {
        return BindingBuilder.bind(enrollmentQueue)
                .to(eventsExchange)
                .with(PAYMENT_CONFIRMED_ROUTING_KEY);
    }
}