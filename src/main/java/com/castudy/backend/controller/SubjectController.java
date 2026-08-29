package com.castudy.backend.controller;

import com.castudy.backend.dto.SubjectRequest;
import com.castudy.backend.entity.Subject;
import com.castudy.backend.service.SubjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping("/subjects")
    public List<Subject> getSubjects() {
        return subjectService.getAllSubjects();
    }

    @PostMapping("/subjects")
    public Subject createSubject(@RequestBody SubjectRequest request) {
        return subjectService.createSubject(request);
    }
}