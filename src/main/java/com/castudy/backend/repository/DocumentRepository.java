package com.castudy.backend.repository;

import com.castudy.backend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findBySubjectId(Long subjectId);
}