package com.castudy.backend.service;

import com.castudy.backend.dto.DeviceTokenRequest;
import com.castudy.backend.entity.DeviceToken;
import com.castudy.backend.entity.Student;
import com.castudy.backend.repository.DeviceTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public void registerToken(
            Student student,
            DeviceTokenRequest request
    ) {

        Optional<DeviceToken> existingToken =
                deviceTokenRepository.findByToken(request.getToken());

        if (existingToken.isPresent()) {

            DeviceToken deviceToken = existingToken.get();

            deviceToken.setStudent(student);
            deviceToken.setPlatform(request.getPlatform());
            deviceToken.setUpdatedAt(Instant.now());

            deviceTokenRepository.save(deviceToken);

            return;
        }

        DeviceToken deviceToken = new DeviceToken(
                student,
                request.getToken(),
                request.getPlatform()
        );

        deviceTokenRepository.save(deviceToken);
    }
}