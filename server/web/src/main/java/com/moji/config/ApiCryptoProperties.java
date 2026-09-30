package com.moji.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "api.crypto")
public class ApiCryptoProperties {

    private String key = "bi-video-api-key-32-byte-value!!";

    private String iv = "bi-video-api-iv!";
}