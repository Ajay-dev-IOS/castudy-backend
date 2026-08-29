package com.castudy.backend.dto;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    public AuthResponse(
            String accessToken,
            String refreshToken,
            long expiresIn) {

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}