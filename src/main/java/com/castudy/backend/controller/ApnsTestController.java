package com.castudy.backend.controller;

import com.castudy.backend.service.ApnsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/apns")
public class ApnsTestController {

    private final ApnsService apnsService;

    public ApnsTestController(ApnsService apnsService) {
        this.apnsService = apnsService;
    }

    @PostMapping("/test")
    public String sendTestNotification(
            @RequestParam String deviceToken
    ) {


        return "Notification request sent";
    }
}