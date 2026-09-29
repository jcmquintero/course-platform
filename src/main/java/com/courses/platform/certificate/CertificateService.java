package com.courses.platform.certificate;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courses.platform.enrollment.Enrollment;
import com.courses.platform.enrollment.EnrollmentRepository;
import com.courses.platform.messaging.ProcessedEvent;
import com.courses.platform.messaging.ProcessedEventRepository;
import com.courses.platform.messaging.event.EnrollmentCompletedEvent;
import com.courses.platform.shared.ResourceNotFoundException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final Counter certificatesIssuedCounter;

    public CertificateService(
            CertificateRepository certificateRepository,
            EnrollmentRepository enrollmentRepository,
            ProcessedEventRepository processedEventRepository,
            MeterRegistry meterRegistry
        ) {
        this.certificateRepository = certificateRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.processedEventRepository = processedEventRepository;
        this.certificatesIssuedCounter = meterRegistry.counter("certificates.issued");
    }

    @Transactional
    public void processEnrollmentCompleted(EnrollmentCompletedEvent event) {

        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        Enrollment enrollment = enrollmentRepository
                .findById(event.enrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found"));

        if (!certificateRepository
                .existsByEnrollmentId(enrollment.getId())) {

            Certificate certificate = new Certificate(
                    enrollment,
                    UUID.randomUUID().toString());

            certificateRepository.save(certificate);
            certificatesIssuedCounter.increment();
        }

        processedEventRepository.save(
                new ProcessedEvent(event.eventId()));
    }
}