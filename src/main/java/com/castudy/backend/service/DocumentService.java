package com.castudy.backend.service;

import com.castudy.backend.dto.DocumentResponse;
import com.castudy.backend.entity.Document;
import com.castudy.backend.entity.DocumentContent;
import com.castudy.backend.entity.Subject;
import com.castudy.backend.repository.DocumentContentRepository;
import com.castudy.backend.repository.DocumentRepository;
import com.castudy.backend.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.castudy.backend.repository.DocumentChunkRepository;
import com.castudy.backend.entity.DocumentChunk;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final DocumentRepository documentRepository;
    private final SubjectRepository subjectRepository;
    private final PdfTextExtractionService pdfTextExtractionService;
    private final DocumentContentRepository documentContentRepository;
    private final TextCleaningService textCleaningService;
    private final TextChunkingService textChunkingService;
    private final DocumentChunkRepository documentChunkRepository;


    @Value("${file.upload-dir}")
    private String uploadDir;

    public DocumentService(
            DocumentRepository documentRepository,
            SubjectRepository subjectRepository,
            PdfTextExtractionService pdfTextExtractionService,
            DocumentContentRepository documentContentRepository,
            TextCleaningService textCleaningService,
            TextChunkingService textChunkingService,
            DocumentChunkRepository documentChunkRepository) {

        this.documentRepository = documentRepository;
        this.subjectRepository = subjectRepository;
        this.pdfTextExtractionService = pdfTextExtractionService;
        this.documentContentRepository = documentContentRepository;
        this.textCleaningService = textCleaningService;
        this.textChunkingService = textChunkingService;
        this.documentChunkRepository = documentChunkRepository;
    }

    @Transactional
    public String extractText(Long documentId) {

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException("Document not found"));

        Path path = Paths.get(document.getFilePath());

        String extractedText =
                pdfTextExtractionService.extractText(path);

        String cleanedText =
                textCleaningService.clean(extractedText);

        // Save extracted text
        DocumentContent documentContent =
                documentContentRepository
                        .findByDocumentId(documentId)
                        .orElseGet(DocumentContent::new);

        documentContent.setDocument(document);
        documentContent.setExtractedText(cleanedText);
        documentContent.setExtractedAt(Instant.now());

        documentContentRepository.save(documentContent);

        // Generate chunks
        List<String> chunks =
                textChunkingService.chunk(cleanedText);

        // Remove old chunks if the document is re-processed
        documentChunkRepository.deleteByDocumentId(documentId);

        // Persist new chunks
        for (int i = 0; i < chunks.size(); i++) {

            DocumentChunk documentChunk =
                    new DocumentChunk();

            documentChunk.setDocument(document);
            documentChunk.setChunkIndex(i);
            documentChunk.setContent(chunks.get(i));

            documentChunkRepository.save(documentChunk);
        }

        return cleanedText;
    }

    public DocumentResponse uploadDocument(
            Long subjectId,
            MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size cannot exceed 20 MB");
        }

        if (!PDF_CONTENT_TYPE.equalsIgnoreCase(
                file.getContentType())) {

            throw new IllegalArgumentException(
                    "Only PDF files are allowed");
        }

        if (file.getOriginalFilename() == null ||
                file.getOriginalFilename().isBlank()) {

            throw new IllegalArgumentException(
                    "File name is required");
        }

        String originalFileName =
                file.getOriginalFilename();

        String storedFileName =
                UUID.randomUUID() + ".pdf";

        Path uploadPath = Paths.get(uploadDir);
        Path targetPath =
                uploadPath.resolve(storedFileName);

        try {
            Files.createDirectories(uploadPath);
            file.transferTo(targetPath);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to store file", e);
        }

        Subject subject =
                subjectRepository.findById(subjectId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subject not found"));

        Document document = new Document();

        document.setFileName(originalFileName);
        document.setFilePath(targetPath.toString());
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setUploadedAt(Instant.now());
        document.setSubject(subject);

        Document savedDocument =
                documentRepository.save(document);

        return new DocumentResponse(
                savedDocument.getId(),
                savedDocument.getFileName(),
                savedDocument.getContentType(),
                savedDocument.getFileSize(),
                savedDocument.getUploadedAt(),
                subject.getId()
        );
    }

    public List<DocumentResponse> getDocumentsBySubject(
            Long subjectId) {

        if (!subjectRepository.existsById(subjectId)) {
            throw new RuntimeException(
                    "Subject not found");
        }

        return documentRepository
                .findBySubjectId(subjectId)
                .stream()
                .map(document -> new DocumentResponse(
                        document.getId(),
                        document.getFileName(),
                        document.getContentType(),
                        document.getFileSize(),
                        document.getUploadedAt(),
                        document.getSubject().getId()
                ))
                .toList();
    }

    public ResponseEntity<Resource> downloadDocument(
            Long documentId) {

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found"));

        Path path =
                Paths.get(document.getFilePath());

        try {
            Resource resource =
                    new UrlResource(path.toUri());

            if (!resource.exists() ||
                    !resource.isReadable()) {

                throw new RuntimeException(
                        "File not found");
            }

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    document.getContentType()
                            )
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" +
                                    document.getFileName() +
                                    "\""
                    )
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to read document", e);
        }
    }

    public void deleteDocument(Long documentId) {

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found"));

        Path path =
                Paths.get(document.getFilePath());

        try {
            Files.deleteIfExists(path);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to delete document file", e);
        }

        documentRepository.delete(document);
    }

    @Transactional
    public List<String> getTextChunks(Long documentId) {

        if (!documentRepository.existsById(documentId)) {
            throw new RuntimeException("Document not found");
        }

        return documentChunkRepository
                .findByDocumentIdOrderByChunkIndexAsc(documentId)
                .stream()
                .map(DocumentChunk::getContent)
                .toList();
    }
}