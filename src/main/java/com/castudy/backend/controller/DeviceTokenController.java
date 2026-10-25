package com.castudy.backend.controller;

import com.castudy.backend.dto.DeviceTokenRequest;
import com.castudy.backend.dto.DeviceTokenResponse;
import com.castudy.backend.entity.Student;
import com.castudy.backend.service.DeviceTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    public DeviceTokenController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @PostMapping("/token")
    public ResponseEntity<DeviceTokenResponse> registerToken(
            @Valid @RequestBody DeviceTokenRequest request,
            Authentication authentication
    ) {

        Student student =
                (Student) authentication.getPrincipal();

        deviceTokenService.registerToken(
                student,
                request
        );

        return ResponseEntity.ok(
                new DeviceTokenResponse("Device token registered")
        );
    }
}