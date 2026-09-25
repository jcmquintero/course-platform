package com.courses.platform.certificate;

import com.courses.platform.enrollment.Enrollment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    @Column(name = "verification_code", nullable = false, unique = true)
    private String verificationCode;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    protected Certificate() {
    }

    public Certificate(Enrollment enrollment, String verificationCode) {
        this.id = UUID.randomUUID();
        this.enrollment = enrollment;
        this.verificationCode = verificationCode;
        this.issuedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}