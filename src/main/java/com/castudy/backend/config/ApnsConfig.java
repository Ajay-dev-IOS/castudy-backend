package com.castudy.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApnsConfig {

    @Value("${apns.team-id}")
    private String teamId;

    @Value("${apns.key-id}")
    private String keyId;

    @Value("${apns.bundle-id}")
    private String bundleId;

    @Value("${apns.key-path}")
    private String keyPath;

    public String getTeamId() {
        return teamId;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getBundleId() {
        return bundleId;
    }

    public String getKeyPath() {
        return keyPath;
    }
}