package com.courses.platform.student;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courses.platform.shared.ConflictException;
import com.courses.platform.student.dto.CreateStudentRequest;
import com.courses.platform.student.dto.StudentResponse;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new ConflictException("Student email already exists: " + request.email());
        }

        Student student = new Student(
                request.firstName(),
                request.lastName(),
                request.email());

        return toResponse(studentRepository.save(student));
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getRegisteredAt());
    }
}