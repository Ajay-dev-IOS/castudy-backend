package com.castudy.backend.dto;

public class DeviceTokenResponse {

    private String message;

    public DeviceTokenResponse() {
    }

    public DeviceTokenResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}