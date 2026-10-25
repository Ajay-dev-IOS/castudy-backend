package com.castudy.backend.service;

import com.castudy.backend.config.ApnsConfig;
import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import org.springframework.stereotype.Service;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;

import java.io.File;

@Service
public class ApnsService {

    private final ApnsClient apnsClient;
    private final ApnsConfig apnsConfig;

    public ApnsService(ApnsConfig apnsConfig) throws Exception {
        this.apnsConfig = apnsConfig;

        ApnsSigningKey signingKey = ApnsSigningKey.loadFromPkcs8File(
                new File(apnsConfig.getKeyPath()),
                apnsConfig.getTeamId(),
                apnsConfig.getKeyId()
        );

        this.apnsClient = new ApnsClientBuilder()
                .setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                .setSigningKey(signingKey)
                .build();
    }

    public void sendNotification(
            String deviceToken,
            String title,
            String message
    ) {

        String payload = String.format(
                "{\"aps\":{\"alert\":{\"title\":\"%s\",\"body\":\"%s\"},\"sound\":\"default\"}}",
                title,
                message
        );

        SimpleApnsPushNotification notification =
                new SimpleApnsPushNotification(
                        deviceToken,
                        apnsConfig.getBundleId(),
                        payload
                );

        apnsClient.sendNotification(notification)
                .whenComplete((response, throwable) -> {

                    if (throwable != null) {
                        System.out.println(
                                "❌ APNs send failed: "
                                        + throwable.getMessage()
                        );
                        return;
                    }

                    if (response.isAccepted()) {
                        System.out.println(
                                "🟢 APNs notification accepted"
                        );
                    } else {
                        System.out.println(
                                "🔴 APNs notification rejected: "
                                        + response.getRejectionReason()
                        );
                    }
                });
    }
}