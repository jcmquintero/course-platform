package com.courses.platform.instructor;

import com.courses.platform.instructor.dto.CreateInstructorRequest;
import com.courses.platform.instructor.dto.InstructorResponse;
import com.courses.platform.shared.ConflictException;
import com.courses.platform.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;

    public InstructorService(InstructorRepository instructorRepository) {
        this.instructorRepository = instructorRepository;
    }

    @Transactional
    public InstructorResponse create(CreateInstructorRequest request) {

        if (instructorRepository.existsByEmail(request.email())) {
            throw new ConflictException(
                    "Instructor email already exists: " + request.email());
        }

        Instructor instructor = new Instructor(
                request.name(),
                request.email(),
                request.bio());

        Instructor savedInstructor = instructorRepository.save(instructor);

        return toResponse(savedInstructor);
    }

    @Transactional(readOnly = true)
    public InstructorResponse findById(UUID id) {
        return toResponse(getInstructor(id));
    }

    private Instructor getInstructor(UUID id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instructor not found: " + id));
    }

    private InstructorResponse toResponse(Instructor instructor) {
        return new InstructorResponse(
                instructor.getId(),
                instructor.getName(),
                instructor.getEmail(),
                instructor.getBio());
    }
}