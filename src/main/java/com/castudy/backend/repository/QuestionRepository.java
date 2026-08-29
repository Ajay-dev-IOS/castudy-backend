package com.castudy.backend.repository;

import com.castudy.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByDocumentChunkId(Long documentChunkId);

    void deleteByDocumentChunkId(Long documentChunkId);
}