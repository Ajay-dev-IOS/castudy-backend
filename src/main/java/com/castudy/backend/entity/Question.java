package com.castudy.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_chunk_id", nullable = false)
    private DocumentChunk documentChunk;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false)
    private String difficulty;

    public Question() {
    }

    public Long getId() {
        return id;
    }

    public DocumentChunk getDocumentChunk() {
        return documentChunk;
    }

    public void setDocumentChunk(DocumentChunk documentChunk) {
        this.documentChunk = documentChunk;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}