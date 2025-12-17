package com.walkit.walkit.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${fcm.firebase.config.path}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
            try (InputStream stream = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(stream))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase app has been initialized successfully.");
                }
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase app", e);
        }
    }
}