package com.castudy.backend.dto;

public class QuestionResponse {

    private Long id;
    private String question;
    private String difficulty;
    private Long documentChunkId;

    public QuestionResponse(
            Long id,
            String question,
            String difficulty,
            Long documentChunkId) {

        this.id = id;
        this.question = question;
        this.difficulty = difficulty;
        this.documentChunkId = documentChunkId;
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

    public Long getDocumentChunkId() {
        return documentChunkId;
    }
}