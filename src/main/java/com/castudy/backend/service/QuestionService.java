package com.castudy.backend.service;

import com.castudy.backend.dto.QuestionResponse;
import com.castudy.backend.entity.DocumentChunk;
import com.castudy.backend.entity.Question;
import com.castudy.backend.repository.DocumentChunkRepository;
import com.castudy.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final AiQuestionGenerationService aiQuestionGenerationService;

    public QuestionService(
            QuestionRepository questionRepository,
            DocumentChunkRepository documentChunkRepository,
            AiQuestionGenerationService aiQuestionGenerationService) {

        this.questionRepository = questionRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.aiQuestionGenerationService = aiQuestionGenerationService;
    }

    public QuestionResponse saveQuestion(
            Long documentChunkId,
            String questionText,
            String difficulty) {

        DocumentChunk documentChunk =
                documentChunkRepository.findById(documentChunkId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document chunk not found"));

        Question question = new Question();

        question.setDocumentChunk(documentChunk);
        question.setQuestion(questionText);
        question.setDifficulty(difficulty);

        Question savedQuestion =
                questionRepository.save(question);

        return new QuestionResponse(
                savedQuestion.getId(),
                savedQuestion.getQuestion(),
                savedQuestion.getDifficulty(),
                documentChunkId
        );
    }

    public List<QuestionResponse> getQuestionsByChunk(
            Long documentChunkId) {

        if (!documentChunkRepository.existsById(documentChunkId)) {
            throw new RuntimeException(
                    "Document chunk not found");
        }

        return questionRepository
                .findByDocumentChunkId(documentChunkId)
                .stream()
                .map(question ->
                        new QuestionResponse(
                                question.getId(),
                                question.getQuestion(),
                                question.getDifficulty(),
                                documentChunkId
                        ))
                .toList();
    }

    public List<QuestionResponse> generateQuestions(Long chunkId) {

        DocumentChunk documentChunk =
                documentChunkRepository.findById(chunkId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document chunk not found"));

        List<String> generatedQuestions =
                aiQuestionGenerationService
                        .generateQuestions(documentChunk);

        List<QuestionResponse> responses = new java.util.ArrayList<>();

        for (String questionText : generatedQuestions) {

            Question question = new Question();

            question.setDocumentChunk(documentChunk);
            question.setQuestion(questionText);
            question.setDifficulty("MEDIUM");

            Question savedQuestion =
                    questionRepository.save(question);

            responses.add(
                    new QuestionResponse(
                            savedQuestion.getId(),
                            savedQuestion.getQuestion(),
                            savedQuestion.getDifficulty(),
                            chunkId
                    )
            );
        }

        return responses;
    }

    public List<QuestionResponse> generateQuestionsForDocument(
            Long documentId,
            int requestedCount) {

        if (requestedCount <= 0) {
            throw new IllegalArgumentException(
                    "Question count must be greater than 0");
        }

        if (requestedCount > 100) {
            throw new IllegalArgumentException(
                    "Question count cannot exceed 100");
        }

        List<DocumentChunk> chunks =
                documentChunkRepository
                        .findByDocumentIdOrderByChunkIndexAsc(documentId);

        if (chunks.isEmpty()) {
            throw new RuntimeException(
                    "No document chunks found");
        }

        List<String> generatedQuestions =
                aiQuestionGenerationService
                        .generateQuestionsFromDocument(
                                chunks,
                                requestedCount
                        );

        List<QuestionResponse> responses =
                new java.util.ArrayList<>();

        for (String questionText : generatedQuestions) {

            Question question = new Question();

            /*
             * At this point the AI question is document-level,
             * so we don't yet know which specific chunk produced it.
             */
            question.setDocumentChunk(null);
            question.setQuestion(questionText);
            question.setDifficulty("MEDIUM");

            Question savedQuestion =
                    questionRepository.save(question);

            responses.add(
                    new QuestionResponse(
                            savedQuestion.getId(),
                            savedQuestion.getQuestion(),
                            savedQuestion.getDifficulty(),
                            null
                    )
            );
        }

        return responses;
    }


}