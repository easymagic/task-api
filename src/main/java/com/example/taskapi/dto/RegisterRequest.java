package com.example.taskapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming JSON for {@code POST /api/auth/register}.
 * <p>
 * A Java {@code record} is an immutable data carrier (Java 16+). The compiler
 * generates the constructor, accessors ({@code email()}, {@code password()},
 * {@code name()}), {@code equals}, {@code hashCode} and {@code toString}.
 * <p>
 * The annotations are Bean Validation constraints. Combined with {@code @Valid}
 * on the controller parameter, they play the role of a Laravel Form Request.
 */
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 255) String name
) {
}
