package com.castudy.backend.service;

import com.castudy.backend.entity.DocumentChunk;

import java.util.List;

public interface AiQuestionGenerationService {

    List<String> generateQuestions(DocumentChunk documentChunk);

    List<String> generateQuestionsFromDocument(
            List<DocumentChunk> chunks,
            int requestedCount);
}