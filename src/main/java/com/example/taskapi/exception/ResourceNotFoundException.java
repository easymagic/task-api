package com.example.taskapi.exception;

/**
 * Thrown when a row is missing, or is not owned by the current user.
 * Mapped to HTTP 404 by {@code GlobalExceptionHandler}.
 * <p>
 * Returning 404 (not 403) for "not yours" avoids leaking that another user's task exists.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
