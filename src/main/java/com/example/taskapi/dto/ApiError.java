package com.example.taskapi.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error body returned by {@code GlobalExceptionHandler}.
 * {@code fieldErrors} is filled for validation failures (key = field, value = message).
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, Map.of());
    }

    public static ApiError of(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, path, fieldErrors);
    }
}
