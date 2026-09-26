package com.courses.platform.instructor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InstructorRepository extends JpaRepository<Instructor, UUID> {

    boolean existsByEmail(String email);
}