CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL
);

CREATE TABLE instructors (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    bio VARCHAR(255)
);

CREATE TABLE courses (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    estimated_hours INTEGER NOT NULL,
    level VARCHAR(50) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    max_seats INTEGER NOT NULL,
    occupied_seats INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    category_id UUID NOT NULL,
    instructor_id UUID NOT NULL,

    CONSTRAINT fk_courses_category
        FOREIGN KEY (category_id) REFERENCES categories(id),

    CONSTRAINT fk_courses_instructor
        FOREIGN KEY (instructor_id) REFERENCES instructors(id),

    CONSTRAINT chk_courses_estimated_hours
        CHECK (estimated_hours > 0),

    CONSTRAINT chk_courses_price
        CHECK (price >= 0),

    CONSTRAINT chk_courses_max_seats
        CHECK (max_seats > 0),

    CONSTRAINT chk_courses_occupied_seats
        CHECK (occupied_seats >= 0 AND occupied_seats <= max_seats),

    CONSTRAINT chk_courses_level
        CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),

    CONSTRAINT chk_courses_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX idx_courses_category_id
    ON courses(category_id);

CREATE INDEX idx_courses_instructor_id
    ON courses(instructor_id);


CREATE TABLE students (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    registered_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE enrollments (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    progress INTEGER NOT NULL,
    enrolled_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,

    CONSTRAINT fk_enrollments_student
        FOREIGN KEY (student_id) REFERENCES students(id),

    CONSTRAINT fk_enrollments_course
        FOREIGN KEY (course_id) REFERENCES courses(id),

    CONSTRAINT chk_enrollments_status
        CHECK (
            status IN (
                'PENDING_PAYMENT',
                'ACTIVE',
                'COMPLETED',
                'CANCELLED'
            )
        ),

    CONSTRAINT chk_enrollments_progress
        CHECK (progress BETWEEN 0 AND 100)
);

CREATE UNIQUE INDEX uq_enrollments_student_course_active
    ON enrollments(student_id, course_id)
    WHERE status <> 'CANCELLED';

CREATE INDEX idx_enrollments_course_id
    ON enrollments(course_id);

CREATE INDEX idx_enrollments_student_id
    ON enrollments(student_id);


CREATE TABLE payments (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL UNIQUE,
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    confirmed_at TIMESTAMPTZ,

    CONSTRAINT fk_payments_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),

    CONSTRAINT chk_payments_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_payments_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'FAILED'))
);


CREATE TABLE certificates (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL UNIQUE,
    verification_code VARCHAR(255) NOT NULL UNIQUE,
    issued_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_certificates_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments(id)
);