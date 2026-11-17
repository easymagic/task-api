package com.example.taskapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.jwt.*} from application.yml (or {@code APP_JWT_SECRET} etc.).
 * {@code @ConfigurationProperties} is the typed alternative to scattering
 * {@code @Value} annotations — closer to a Laravel config file class.
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long expirationMs = 86_400_000L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
