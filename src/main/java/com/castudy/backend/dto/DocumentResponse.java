package com.castudy.backend.dto;

import java.time.Instant;

public class DocumentResponse {

    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private Instant uploadedAt;
    private Long subjectId;

    public DocumentResponse(
            Long id,
            String fileName,
            String contentType,
            Long fileSize,
            Instant uploadedAt,
            Long subjectId) {

        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
        this.subjectId = subjectId;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public Long getSubjectId() {
        return subjectId;
    }
}