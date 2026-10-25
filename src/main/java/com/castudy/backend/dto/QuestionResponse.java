package com.castudy.backend.dto;

public class QuestionResponse {

    private Long id;
    private String question;
    private String difficulty;
    private Long documentId;

    public QuestionResponse(
            Long id,
            String question,
            String difficulty,
            Long documentId) {

        this.id = id;
        this.question = question;
        this.difficulty = difficulty;
        this.documentId = documentId;
    }

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Long getDocumentId() {
        return documentId;
    }
}