package com.courses.platform.instructor;

import com.courses.platform.instructor.dto.CreateInstructorRequest;
import com.courses.platform.instructor.dto.InstructorResponse;
import com.courses.platform.instructor.dto.UpdateInstructorRequest;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/instructors")
public class InstructorController {

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstructorResponse create(
            @Valid @RequestBody CreateInstructorRequest request) {

        return instructorService.create(request);
    }

    @GetMapping("/{id}")
    public InstructorResponse findById(@PathVariable UUID id) {
        return instructorService.findById(id);
    }

    @GetMapping
    public Page<InstructorResponse> findAll(Pageable pageable) {
        return instructorService.findAll(pageable);
    }

    @PutMapping("/{id}")
    public InstructorResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInstructorRequest request) {

        return instructorService.update(id, request);
    }
}