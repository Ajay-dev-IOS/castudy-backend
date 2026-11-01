
package com.castudy.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SupabaseStorageService {

    @Value("${SUPABASE_URL}")
    private String supabaseUrl;

    @Value("${SUPABASE_SECRET_KEY}")
    private String secretKey;

    private static final String BUCKET_NAME = "documents";

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    public void uploadFile(
            byte[] fileBytes,
            String fileName,
            String contentType) {

        String url = supabaseUrl
                + "/storage/v1/object/"
                + BUCKET_NAME + "/" + fileName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", secretKey)
                .header("Authorization", "Bearer " + secretKey)
                .header("Content-Type", contentType)
                .header("x-upsert", "false")
                .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Supabase upload failed: "
                                + response.body()
                );
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload PDF to Supabase", e
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Supabase upload interrupted", e
            );
        }
    }

    public byte[] downloadFile(String fileName) {

        String url = supabaseUrl
                + "/storage/v1/object/"
                + BUCKET_NAME + "/" + fileName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", secretKey)
                .header("Authorization", "Bearer " + secretKey)
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofByteArray()
                    );

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Supabase download failed. HTTP status: "
                                + response.statusCode()
                );
            }

            return response.body();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to download PDF from Supabase", e
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Supabase download interrupted", e
            );
        }
    }

    public void deleteFile(String fileName) {

        String url = supabaseUrl
                + "/storage/v1/object/"
                + BUCKET_NAME + "/" + fileName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", secretKey)
                .header("Authorization", "Bearer " + secretKey)
                .DELETE()
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Supabase delete failed: "
                                + response.body()
                );
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to delete PDF from Supabase", e
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Supabase delete interrupted", e
            );
        }
    }
}
