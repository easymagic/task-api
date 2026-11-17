package com.example.taskapi.dto;

/**
 * JSON returned after a successful register or login.
 * {@code tokenType} is always {@code Bearer}, so clients can send
 * {@code Authorization: Bearer <token>}.
 */
public record AuthResponse(String token, String tokenType, String email, String name) {

    public AuthResponse(String token, String email, String name) {
        this(token, "Bearer", email, name);
    }
}
