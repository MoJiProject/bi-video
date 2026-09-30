package com.moji.config;

import com.moji.serve.LoginLimiterServer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginLimiterRedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @PostConstruct
    public void configureLoginLimiterRedis() {
        LoginLimiterServer.configureRedis(host, port, password);
    }
}