package com.courses.platform.instructor;

import com.courses.platform.instructor.dto.CreateInstructorRequest;
import com.courses.platform.instructor.dto.InstructorResponse;
import com.courses.platform.instructor.dto.UpdateInstructorRequest;
import com.courses.platform.shared.ConflictException;
import com.courses.platform.shared.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<InstructorResponse> findAll(Pageable pageable) {
        return instructorRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public InstructorResponse update(UUID id, UpdateInstructorRequest request) {

        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        if (instructorRepository.existsByEmailAndIdNot(
                request.email(),
                id)) {
            throw new ConflictException(
                    "Instructor email already exists");
        }

        instructor.update(
                request.name(),
                request.email(),
                request.bio());

        return toResponse(instructor);
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