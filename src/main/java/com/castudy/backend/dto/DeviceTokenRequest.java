package com.castudy.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class DeviceTokenRequest {

    @NotBlank
    private String token;

    @NotBlank
    private String platform;

    public DeviceTokenRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}