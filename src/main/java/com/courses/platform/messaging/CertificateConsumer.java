package com.courses.platform.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.courses.platform.certificate.CertificateService;
import com.courses.platform.messaging.event.EnrollmentCompletedEvent;

import tools.jackson.databind.ObjectMapper;

@Component
public class CertificateConsumer {

    private final ObjectMapper objectMapper;
    private final CertificateService certificateService;

    public CertificateConsumer(
            ObjectMapper objectMapper,
            CertificateService certificateService) {
        this.objectMapper = objectMapper;
        this.certificateService = certificateService;
    }

    @RabbitListener(queues = RabbitMqConfig.CERTIFICATE_QUEUE)
    public void consume(String payload) {
        EnrollmentCompletedEvent event = objectMapper.readValue(
                payload,
                EnrollmentCompletedEvent.class);

        certificateService.processEnrollmentCompleted(event);
    }
}