package com.castudy.backend.controller;

import com.castudy.backend.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentDownloadController {

    private final DocumentService documentService;

    public DocumentDownloadController(
            DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping(
            value = "/{documentId}/download",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId) {

        return documentService.downloadDocument(documentId);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long documentId) {

        documentService.deleteDocument(documentId);

        return ResponseEntity.noContent().build();
    }
}