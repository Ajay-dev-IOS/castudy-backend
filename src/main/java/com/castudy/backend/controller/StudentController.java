package com.castudy.backend.controller;

import com.castudy.backend.dto.*;
import com.castudy.backend.entity.Student;
import com.castudy.backend.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public StudentResponse save(@Valid @RequestBody StudentRequest student) {
        return service.save(student);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }
}