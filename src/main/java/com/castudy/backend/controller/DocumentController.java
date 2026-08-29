package com.castudy.backend.controller;

import com.castudy.backend.dto.DocumentResponse;
import com.castudy.backend.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subjects")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(
            value = "/{subjectId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DocumentResponse uploadDocument(
            @PathVariable Long subjectId,
            @RequestParam("file") MultipartFile file) {

        return documentService.uploadDocument(
                subjectId,
                file
        );
    }

    @GetMapping("/{subjectId}/documents")
    public List<DocumentResponse> getDocuments(
            @PathVariable Long subjectId) {

        return documentService.getDocumentsBySubject(subjectId);
    }

    @GetMapping("/{documentId}/text")
    public String extractText(
            @PathVariable Long documentId) {

        return documentService.extractText(documentId);
    }

    @GetMapping("/{documentId}/chunks")
    public List<String> getChunks(
            @PathVariable Long documentId) {

        System.out.println("===== CHUNKS CONTROLLER HIT =====");
        System.out.println("Document ID: " + documentId);

        List<String> chunks = documentService.getTextChunks(documentId);

        System.out.println("Total chunks: " + chunks.size());

        return chunks;
    }
}