package com.walkit.walkit.global.config;

import com.walkit.walkit.domain.weather.dto.CurrentWeatherResponseDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;


@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, CurrentWeatherResponseDto> weatherRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, CurrentWeatherResponseDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // key
        template.setKeySerializer(RedisSerializer.string());

        // value
        template.setValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }
}


