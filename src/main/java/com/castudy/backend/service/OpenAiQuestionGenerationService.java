package com.castudy.backend.service;

import com.castudy.backend.entity.DocumentChunk;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAiQuestionGenerationService
        implements AiQuestionGenerationService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    public OpenAiQuestionGenerationService(
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    @Override
    public List<String> generateQuestions(
            DocumentChunk documentChunk) {

        String prompt = """
        You are an expert CA and CMA examination question setter.

        Generate exactly 3 high-quality exam-oriented questions
        from the provided study material.

        STRICT RULES:

        1. Use ONLY information contained in the study material.
        2. Every question must be answerable using the provided material.
        3. Do not invent facts, concepts, examples, formulas, or numerical data.
        4. Do not ask questions about information that is not provided.
        5. Do not simply convert a sentence from the material into a question.
        6. Questions should test understanding, explanation, analysis,
           application, or calculation where appropriate.
        7. Do not create a calculation question unless the material
           provides enough information to perform the calculation.
        8. Do not combine unrelated concepts into one question.
        9. Avoid duplicate or substantially similar questions.
        10. Keep each question clear, concise, and suitable for CA
            examination preparation.
        11. Give preference to important concepts, definitions,
            applications, comparisons, and exam-relevant areas.
        12. If the material contains a practical situation, prefer
            an application-oriented question where appropriate.
        13. Do not provide answers.
        14. Do not number the questions.
        15. Return exactly 3 questions.
        16. Return one question per line and nothing else.

        Study material:

        %s
        """.formatted(documentChunk.getContent());

        String requestBody = """
                {
                  "model": "%s",
                  "reasoning": {
                    "effort": "none"
                  },
                  "input": %s,
                  "text": {
                    "format": {
                      "type": "json_schema",
                      "name": "question_generation",
                      "strict": true,
                      "schema": {
                        "type": "object",
                        "properties": {
                          "questions": {
                            "type": "array",
                            "items": {
                              "type": "string"
                            },
                            "minItems": 3,
                            "maxItems": 3
                          }
                        },
                        "required": ["questions"],
                        "additionalProperties": false
                      }
                    }
                  }
                }
                """.formatted(
                model,
                toJsonString(prompt)
        );

        String response = restClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        System.out.println("===== OPENAI RESPONSE =====");
        System.out.println(response);
        System.out.println("====================================");

        return parseQuestions(response);
    }

    private List<String> parseQuestions(String response) {

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode output = root.path("output");

            if (!output.isArray()) {
                throw new RuntimeException(
                        "OpenAI response does not contain an output array"
                );
            }

            for (JsonNode outputItem : output) {

                JsonNode content = outputItem.path("content");

                if (!content.isArray()) {
                    continue;
                }

                for (JsonNode contentItem : content) {

                    JsonNode textNode = contentItem.path("text");

                    if (textNode.isMissingNode() || textNode.isNull()) {
                        continue;
                    }

                    String text = textNode.asText();

                    if (text == null || text.isBlank()) {
                        continue;
                    }

                    JsonNode questionJson =
                            objectMapper.readTree(text);

                    JsonNode questions =
                            questionJson.path("questions");

                    if (!questions.isArray()) {
                        continue;
                    }

                    List<String> result = new ArrayList<>();

                    for (JsonNode question : questions) {

                        if (!question.isTextual()) {
                            throw new RuntimeException(
                                    "Question value is not a string"
                            );
                        }

                        String questionText =
                                question.asText().trim();

                        if (!questionText.isEmpty()) {
                            result.add(questionText);
                        }
                    }

                    if (result.isEmpty()) {
                        throw new RuntimeException(
                                "OpenAI returned an empty questions array"
                        );
                    }

                    return result;
                }
            }

            throw new RuntimeException(
                    "Questions not found in OpenAI response"
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse OpenAI response",
                    e
            );
        }
    }

    private String toJsonString(String text) {

        try {
            return objectMapper.writeValueAsString(text);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create JSON",
                    e
            );
        }
    }

    @Override
    public List<String> generateQuestionsFromDocument(
            List<DocumentChunk> chunks,
            int requestedCount) {

        StringBuilder studyMaterial = new StringBuilder();

        for (DocumentChunk chunk : chunks) {
            studyMaterial
                    .append("\n\n--- CHUNK ")
                    .append(chunk.getChunkIndex())
                    .append(" ---\n\n")
                    .append(chunk.getContent());
        }

        String prompt = """
        You are an expert CA examination question setter.

        You are given the COMPLETE study material from a CA
        examination document.

        Generate exactly %d high-quality exam-oriented questions.

        STRICT RULES:

        1. Use ONLY information contained in the provided material.
        2. Every question must be answerable using the provided material.
        3. Do not invent facts, concepts, examples, formulas,
           assumptions, or numerical data.
        4. Do not ask about information that is not present.
        5. Do not simply convert sentences into questions.
        6. Questions should test understanding, application,
           analysis, comparison, or calculation where appropriate.
        7. Do not create calculations unless sufficient information
           is provided in the material.
        8. Do not combine unrelated concepts into one question.
        9. Avoid duplicate or substantially similar questions.
        10. Cover different important areas of the document.
        11. Do not concentrate questions on the beginning of the document.
        12. Give preference to important CA examination concepts.
        13. Use practical/application questions when the material
            supports them.
        14. Do not provide answers.
        15. Do not number the questions.
        16. Return exactly %d questions.

        COMPLETE STUDY MATERIAL:

        %s
        """.formatted(
                requestedCount,
                requestedCount,
                studyMaterial
        );

        String requestBody = """
            {
              "model": "%s",
              "reasoning": {
                "effort": "none"
              },
              "input": %s,
              "text": {
                "format": {
                  "type": "json_schema",
                  "name": "question_generation",
                  "strict": true,
                  "schema": {
                    "type": "object",
                    "properties": {
                      "questions": {
                        "type": "array",
                        "items": {
                          "type": "string"
                        },
                        "minItems": %d,
                        "maxItems": %d
                      }
                    },
                    "required": ["questions"],
                    "additionalProperties": false
                  }
                }
              }
            }
            """.formatted(
                model,
                toJsonString(prompt),
                requestedCount,
                requestedCount
        );

        String response = restClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        System.out.println("===== OPENAI DOCUMENT RESPONSE =====");
        System.out.println(response);

        return parseQuestions(response);
    }
}