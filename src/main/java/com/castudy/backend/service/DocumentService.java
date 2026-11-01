
package com.castudy.backend.service;

import com.castudy.backend.dto.DocumentResponse;
import com.castudy.backend.entity.Document;
import com.castudy.backend.entity.DocumentContent;
import com.castudy.backend.entity.DocumentChunk;
import com.castudy.backend.entity.Subject;

import com.castudy.backend.repository.DocumentContentRepository;
import com.castudy.backend.repository.DocumentRepository;
import com.castudy.backend.repository.SubjectRepository;
import com.castudy.backend.repository.DocumentChunkRepository;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final SupabaseStorageService supabaseStorageService;

    public DocumentService(
            DocumentRepository documentRepository,
            SubjectRepository subjectRepository,
            PdfTextExtractionService pdfTextExtractionService,
            DocumentContentRepository documentContentRepository,
            TextCleaningService textCleaningService,
            TextChunkingService textChunkingService,
            DocumentChunkRepository documentChunkRepository,
            SupabaseStorageService supabaseStorageService) {

        this.documentRepository = documentRepository;
        this.subjectRepository = subjectRepository;
        this.pdfTextExtractionService = pdfTextExtractionService;
        this.documentContentRepository = documentContentRepository;
        this.textCleaningService = textCleaningService;
        this.textChunkingService = textChunkingService;
        this.documentChunkRepository = documentChunkRepository;
        this.supabaseStorageService = supabaseStorageService;
    }

    // Extract PDF text and generate chunks
    @Transactional
    public String extractText(Long documentId) {

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException("Document not found"));

        // Download PDF from Supabase
        byte[] pdfBytes =
                supabaseStorageService.downloadFile(
                        document.getFilePath()
                );

        // Extract text from PDF bytes
        String extractedText =
                pdfTextExtractionService.extractText(pdfBytes);

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

        // Remove old chunks if document is reprocessed
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

    // Upload PDF to Supabase and save metadata in PostgreSQL
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

        Subject subject =
                subjectRepository.findById(subjectId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subject not found"));

        String originalFileName =
                file.getOriginalFilename();

        String storedFileName =
                UUID.randomUUID() + ".pdf";

        try {
            byte[] fileBytes = file.getBytes();

            // Upload PDF to Supabase Storage
            supabaseStorageService.uploadFile(
                    fileBytes,
                    storedFileName,
                    file.getContentType()
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to read uploaded PDF", e);
        }

        // Save document metadata in PostgreSQL
        Document document = new Document();

        document.setFileName(originalFileName);

        // Store Supabase object name instead of local file path
        document.setFilePath(storedFileName);

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

    // Get documents by subject
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

    // Download PDF from Supabase
    public ResponseEntity<Resource> downloadDocument(
            Long documentId) {

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found"));

        byte[] fileBytes =
                supabaseStorageService.downloadFile(
                        document.getFilePath()
                );

        Resource resource =
                new ByteArrayResource(fileBytes);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                document.getContentType()
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                document.getFileName() + "\""
                )
                .body(resource);
    }

    // Delete PDF from Supabase and metadata from PostgreSQL
    public void deleteDocument(Long documentId) {

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found"));

        // Delete PDF from Supabase
        supabaseStorageService.deleteFile(
                document.getFilePath()
        );

        // Delete document metadata from PostgreSQL
        documentRepository.delete(document);
    }

    // Get document text chunks
    @Transactional
    public List<String> getTextChunks(Long documentId) {

        if (!documentRepository.existsById(documentId)) {
            throw new RuntimeException(
                    "Document not found");
        }

        return documentChunkRepository
                .findByDocumentIdOrderByChunkIndexAsc(documentId)
                .stream()
                .map(DocumentChunk::getContent)
                .toList();
    }
}
