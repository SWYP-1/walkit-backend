package com.walkit.walkit.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Value("${app.urls.production}")
    private String productionUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 자격 증명을 포함한 요청을 위해 특정 오리진 설정
        configuration.setAllowCredentials(true);
        configuration.addAllowedOriginPattern("http://localhost:*");
        configuration.addAllowedOriginPattern("https://localhost:*");
        configuration.addAllowedOrigin(productionUrl);

        // Apple OAuth form_post 요청을 위한 null origin 허용
        configuration.addAllowedOriginPattern("null");

        // 모든 헤더와 메서드 허용
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");

        // 프리플라이트 요청 캐시 시간 설정 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}