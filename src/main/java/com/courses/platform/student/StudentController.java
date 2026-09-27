package com.courses.platform.student;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.courses.platform.student.dto.CreateStudentRequest;
import com.courses.platform.student.dto.StudentResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody CreateStudentRequest request) {
        return studentService.create(request);
    }
}