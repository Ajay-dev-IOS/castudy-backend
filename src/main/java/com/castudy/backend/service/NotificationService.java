package com.castudy.backend.service;

import com.castudy.backend.dto.NotificationResponse;
import com.castudy.backend.entity.DeviceToken;
import com.castudy.backend.entity.Notification;
import com.castudy.backend.entity.Student;
import com.castudy.backend.repository.DeviceTokenRepository;
import com.castudy.backend.repository.NotificationRepository;
import com.castudy.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final ApnsService apnsService;

    public NotificationService(
            NotificationRepository notificationRepository,
            StudentRepository studentRepository,
            DeviceTokenRepository deviceTokenRepository,
            ApnsService apnsService
    ) {
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.apnsService = apnsService;
    }

    public NotificationResponse createNotification(
            Long studentId,
            String title,
            String message,
            String type
    ) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        Notification notification = new Notification(
                student,
                title,
                message,
                type
        );

        Notification savedNotification =
                notificationRepository.save(notification);

        // Send push notification to all devices of this student
        List<DeviceToken> deviceTokens =
                deviceTokenRepository.findByStudentId(studentId);

        for (DeviceToken deviceToken : deviceTokens) {

            apnsService.sendNotification(
                    deviceToken.getToken(),
                    title,
                    message
            );
        }

        return toResponse(savedNotification);
    }

    public List<NotificationResponse> getNotifications(
            Long studentId
    ) {

        return notificationRepository
                .findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public NotificationResponse markAsRead(
            Long notificationId,
            Long studentId
    ) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                ));

        if (!notification.getStudent().getId().equals(studentId)) {
            throw new RuntimeException(
                    "Notification does not belong to this student"
            );
        }

        notification.setRead(true);

        Notification savedNotification =
                notificationRepository.save(notification);

        return toResponse(savedNotification);
    }

    private NotificationResponse toResponse(
            Notification notification
    ) {

        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}