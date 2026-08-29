package com.castudy.backend.controller;

import com.castudy.backend.dto.RefreshResponse;
import com.castudy.backend.dto.RefreshTokenRequest;
import com.castudy.backend.entity.RefreshToken;
import com.castudy.backend.security.JwtService;
import com.castudy.backend.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthController(
            RefreshTokenService refreshTokenService,
            JwtService jwtService) {

        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(
                        request.getRefreshToken()
                );

        String accessToken =
                jwtService.generateToken(
                        refreshToken.getStudent()
                );

        String newRefreshToken =
                refreshTokenService.rotateRefreshToken(
                        refreshToken
                );

        return new RefreshResponse(
                accessToken,
                newRefreshToken,
                900
        );
    }
}