package com.walkit.walkit.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "apple")
@Getter
@Setter
public class AppleProperties {
    private String teamId;
    private String loginKey;
    private String clientId;
    private String redirectUrl;
    private String keyPath;
}