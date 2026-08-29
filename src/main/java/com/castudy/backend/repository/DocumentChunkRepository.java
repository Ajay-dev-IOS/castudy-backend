package com.castudy.backend.repository;

import com.castudy.backend.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(
            Long documentId
    );

    void deleteByDocumentId(Long documentId);
}