package com.castudy.backend.service;

import com.castudy.backend.entity.RefreshToken;
import com.castudy.backend.entity.Student;
import com.castudy.backend.exception.InvalidRefreshTokenException;
import com.castudy.backend.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createRefreshToken(Student student) {

        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String tokenHash = sha256(rawToken);

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setTokenHash(tokenHash);
        refreshToken.setStudent(student);
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(
                Instant.now().plus(
                        refreshTokenExpirationDays,
                        ChronoUnit.DAYS
                )
        );
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm not available", e
            );
        }
    }

    public RefreshToken verifyRefreshToken(String rawToken) {

        String tokenHash = sha256(rawToken);

        RefreshToken refreshToken =
                refreshTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException(
                                        "Invalid refresh token"
                                ));

        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }

    public String rotateRefreshToken(RefreshToken oldToken) {

        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        return createRefreshToken(oldToken.getStudent());
    }
}