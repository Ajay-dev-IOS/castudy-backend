package com.castudy.backend.service;

import com.castudy.backend.dto.SubjectRequest;
import com.castudy.backend.entity.Subject;
import com.castudy.backend.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject createSubject(SubjectRequest request) {

        Subject subject = new Subject();

        subject.setName(request.getName());
        subject.setDescription(request.getDescription());

        return subjectRepository.save(subject);
    }
}