package com.castudy.backend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkingService {

    private static final int CHUNK_SIZE = 8000;
    private static final int OVERLAP_SIZE = 500;

    public List<String> chunk(String text) {

        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        int start = 0;

        while (start < text.length()) {

            int end = Math.min(
                    start + CHUNK_SIZE,
                    text.length()
            );

            String chunk = text.substring(start, end).trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            if (end == text.length()) {
                break;
            }

            start = end - OVERLAP_SIZE;
        }

        return chunks;
    }
}