package com.walkit.walkit.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walkit.walkit.domain.notification.interceptor.LastAccessInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LastAccessInterceptor lastAccessInterceptor;
    private final ObjectMapper objectMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(lastAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/health",
                        "/actuator/**"
                );
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // MultipartJackson2HttpMessageConverter를 리스트 맨 앞에 추가하여 우선순위 높임
        converters.add(0, new MultipartJackson2HttpMessageConverter(objectMapper));
    }
}
