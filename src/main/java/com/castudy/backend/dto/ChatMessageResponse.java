package com.castudy.backend.dto;

import java.time.Instant;

public class ChatMessageResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String message;
    private Instant createdAt;

    public ChatMessageResponse(
            Long id,
            Long studentId,
            String studentName,
            String message,
            Instant createdAt
    ) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}