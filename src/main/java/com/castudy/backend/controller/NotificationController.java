package com.castudy.backend.controller;

import com.castudy.backend.dto.NotificationResponse;
import com.castudy.backend.entity.Student;
import com.castudy.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            Authentication authentication
    ) {

        Student student =
                (Student) authentication.getPrincipal();

        return ResponseEntity.ok(
                notificationService.getNotifications(
                        student.getId()
                )
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {

        Student student =
                (Student) authentication.getPrincipal();

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        notificationId,
                        student.getId()
                )
        );
    }
}