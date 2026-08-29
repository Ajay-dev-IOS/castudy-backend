package com.castudy.backend.service;

import com.castudy.backend.dto.*;
import com.castudy.backend.entity.Role;
import com.castudy.backend.entity.Student;
import com.castudy.backend.exception.EmailAlreadyExistsException;
import com.castudy.backend.repository.StudentRepository;
import com.castudy.backend.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository repository;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public StudentService(
            StudentRepository repository,
            BCryptPasswordEncoder encoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {

        this.repository = repository;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public StudentResponse save(StudentRequest studentReq) {
        if (repository.findByEmail(studentReq.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
       Student student = new Student();
       student.setEmail(studentReq.getEmail());
       student.setName(studentReq.getName());
        student.setPassword(
                encoder.encode(studentReq.getPassword())
        );
        student.setRole(Role.ROLE_STUDENT);
       Student savedStudent = repository.save(student);
       return new StudentResponse(savedStudent.getId(),
               savedStudent.getName(),
               savedStudent.getEmail());

    }

    public AuthResponse login(LoginRequest request) {

        Student student = repository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        System.out.println("Entered Email    : " + request.getEmail());
        System.out.println("DB Email         : " + student.getEmail());

        System.out.println("Entered Password : " + request.getPassword());
        System.out.println("Stored Password  : " + student.getPassword());

        boolean match = encoder.matches(
                request.getPassword(),
                student.getPassword()
        );

        System.out.println("Password Match = " + match);

        if (!match) {
            throw new RuntimeException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(student);

        String refreshToken =
                refreshTokenService.createRefreshToken(student);

        return new AuthResponse(
                accessToken,
                refreshToken,
                900
        );
    }
}