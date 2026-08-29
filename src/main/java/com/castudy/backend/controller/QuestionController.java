package com.castudy.backend.controller;

import com.castudy.backend.dto.QuestionResponse;
import com.castudy.backend.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/chunks/{chunkId}")
    public QuestionResponse saveQuestion(
            @PathVariable Long chunkId,
            @RequestParam String question,
            @RequestParam String difficulty) {

        return questionService.saveQuestion(
                chunkId,
                question,
                difficulty
        );
    }

    @GetMapping("/chunks/{chunkId}")
    public List<QuestionResponse> getQuestionsByChunk(
            @PathVariable Long chunkId) {

        return questionService.getQuestionsByChunk(chunkId);
    }

    @PostMapping("/generate/chunks/{chunkId}")
    public List<QuestionResponse> generateQuestions(
            @PathVariable Long chunkId) {

        return questionService.generateQuestions(chunkId);
    }

    @PostMapping("/generate/documents/{documentId}")
    public List<QuestionResponse> generateQuestionsForDocument(
            @PathVariable Long documentId,
            @RequestParam(defaultValue = "30") int count) {

        return questionService.generateQuestionsForDocument(
                documentId,
                count
        );
    }
}