package com.courses.platform.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import com.courses.platform.category.Category;
import com.courses.platform.category.CategoryRepository;
import com.courses.platform.course.Course;
import com.courses.platform.course.CourseLevel;
import com.courses.platform.course.CourseRepository;
import com.courses.platform.enrollment.Enrollment;
import com.courses.platform.enrollment.EnrollmentRepository;
import com.courses.platform.instructor.Instructor;
import com.courses.platform.instructor.InstructorRepository;
import com.courses.platform.messaging.event.EnrollmentCreatedEvent;
import com.courses.platform.payment.Payment;
import com.courses.platform.payment.PaymentRepository;
import com.courses.platform.payment.PaymentStatus;
import com.courses.platform.student.Student;
import com.courses.platform.student.StudentRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@Testcontainers
class MessagingIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-management-alpine");

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    InstructorRepository instructorRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    ProcessedEventRepository processedEventRepository;

    @Test
    void shouldProcessEnrollmentCreatedOnlyOnce() throws Exception {
        Category category = categoryRepository.save(
                new Category(
                        "Messaging " + UUID.randomUUID(),
                        "Messaging test"));

        Instructor instructor = instructorRepository.save(
                new Instructor(
                        "Test Instructor",
                        UUID.randomUUID() + "@example.com",
                        "Test"));

        Course course = new Course(
                "RabbitMQ Test",
                "Messaging integration test",
                5,
                CourseLevel.BEGINNER,
                new BigDecimal("25.00"),
                5,
                category,
                instructor);

        course.publish();
        courseRepository.save(course);

        Student student = studentRepository.save(
                new Student(
                        "Test",
                        "Student",
                        UUID.randomUUID() + "@example.com"));

        Enrollment enrollment = enrollmentRepository.save(
                new Enrollment(
                        student,
                        course,
                        "messaging-" + UUID.randomUUID()));

        Payment payment = paymentRepository.save(
                new Payment(
                        enrollment,
                        course.getPrice(),
                        "EUR",
                        "payment-" + UUID.randomUUID()));

        UUID eventId = UUID.randomUUID();

        EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(
                eventId,
                1,
                Instant.now(),
                enrollment.getId(),
                student.getId(),
                course.getId(),
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency());

        String payload = objectMapper.writeValueAsString(event);

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EVENTS_EXCHANGE,
                RabbitMqConfig.ENROLLMENT_CREATED_ROUTING_KEY,
                payload);

        waitUntilPaymentConfirmed(payment.getId());

        assertTrue(processedEventRepository.existsById(eventId));

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EVENTS_EXCHANGE,
                RabbitMqConfig.ENROLLMENT_CREATED_ROUTING_KEY,
                payload);

        waitUntilProcessedAgain(eventId);

        Payment persistedPayment = paymentRepository.findById(payment.getId()).orElseThrow();

        assertEquals(
                PaymentStatus.CONFIRMED,
                persistedPayment.getStatus());

        long processedCount = processedEventRepository.findAll()
                .stream()
                .filter(processed -> processed.getEventId().equals(eventId))
                .count();

        assertEquals(1, processedCount);
    }

    private void waitUntilPaymentConfirmed(UUID paymentId)
            throws InterruptedException {

        for (int attempt = 0; attempt < 50; attempt++) {
            Payment payment = paymentRepository
                    .findById(paymentId)
                    .orElseThrow();

            if (payment.getStatus() == PaymentStatus.CONFIRMED) {
                return;
            }

            Thread.sleep(Duration.ofMillis(100));
        }

        throw new AssertionError(
                "Payment was not confirmed in time");
    }

    private void waitUntilProcessedAgain(UUID eventId)
            throws InterruptedException {

        for (int attempt = 0; attempt < 20; attempt++) {
            if (processedEventRepository.existsById(eventId)) {
                Thread.sleep(Duration.ofMillis(100));
                return;
            }

            Thread.sleep(Duration.ofMillis(100));
        }

        throw new AssertionError(
                "Event was not processed in time");
    }

}