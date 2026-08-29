package com.castudy.backend.service;

import org.springframework.stereotype.Service;

@Service
public class TextCleaningService {

    public String clean(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \t]+", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}