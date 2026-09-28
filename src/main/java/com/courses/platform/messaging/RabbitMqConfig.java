package com.courses.platform.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
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

    public static final String ENROLLMENT_COMPLETED_ROUTING_KEY = "enrollment.completed";

    public static final String CERTIFICATE_QUEUE = "certificate.queue";

    public static final String DEAD_LETTER_EXCHANGE = "course.events.dlx";

    public static final String PAYMENT_DLQ = "payment.dlq";

    public static final String ENROLLMENT_DLQ = "enrollment.dlq";

    public static final String CERTIFICATE_DLQ = "certificate.dlq";

    public static final String PAYMENT_DEAD_ROUTING_KEY = "payment.dead";

    public static final String ENROLLMENT_DEAD_ROUTING_KEY = "enrollment.dead";

    public static final String CERTIFICATE_DEAD_ROUTING_KEY = "certificate.dead";

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE);
    }

    @Bean
    Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(PAYMENT_DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    Binding paymentConfirmedBinding(
            Queue enrollmentQueue,
            TopicExchange eventsExchange) {
        return BindingBuilder.bind(enrollmentQueue)
                .to(eventsExchange)
                .with(PAYMENT_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    Queue enrollmentQueue() {
        return QueueBuilder.durable(ENROLLMENT_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(ENROLLMENT_DEAD_ROUTING_KEY)
                .build();
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
    Binding enrollmentCompletedBinding(
            Queue certificateQueue,
            TopicExchange eventsExchange) {
        return BindingBuilder.bind(certificateQueue)
                .to(eventsExchange)
                .with(ENROLLMENT_COMPLETED_ROUTING_KEY);
    }

    @Bean
    Queue certificateQueue() {
        return QueueBuilder.durable(CERTIFICATE_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(CERTIFICATE_DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    Queue paymentDlq() {
        return QueueBuilder.durable(PAYMENT_DLQ).build();
    }

    @Bean
    Queue enrollmentDlq() {
        return QueueBuilder.durable(ENROLLMENT_DLQ).build();
    }

    @Bean
    Queue certificateDlq() {
        return QueueBuilder.durable(CERTIFICATE_DLQ).build();
    }

    @Bean
    Binding paymentDlqBinding(
            Queue paymentDlq,
            DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(paymentDlq)
                .to(deadLetterExchange)
                .with(PAYMENT_DEAD_ROUTING_KEY);
    }

    @Bean
    Binding enrollmentDlqBinding(
            Queue enrollmentDlq,
            DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(enrollmentDlq)
                .to(deadLetterExchange)
                .with(ENROLLMENT_DEAD_ROUTING_KEY);
    }

    @Bean
    Binding certificateDlqBinding(
            Queue certificateDlq,
            DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(certificateDlq)
                .to(deadLetterExchange)
                .with(CERTIFICATE_DEAD_ROUTING_KEY);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }
}