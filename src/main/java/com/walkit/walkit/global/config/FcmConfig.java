package com.walkit.walkit.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${fcm.firebase.config.path:}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        if (firebaseConfigPath == null || firebaseConfigPath.isBlank()) {
            log.warn("fcm.firebase.config.path is missing. Skip Firebase init.");
            return;
        }

        try (InputStream stream = resolveStream(firebaseConfigPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(stream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase app has been initialized successfully.");
            }
        } catch (Exception e) {
            log.error("Error initializing Firebase app", e);
        }
    }

    private InputStream resolveStream(String path) throws IOException {
        java.io.File f = new java.io.File(path);
        if (f.exists() && f.isFile()) {
            return new java.io.FileInputStream(f);
        }
        return new ClassPathResource(path).getInputStream();
    }
}
