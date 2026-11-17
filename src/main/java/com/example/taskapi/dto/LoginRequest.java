package com.example.taskapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Incoming JSON for {@code POST /api/auth/login}. */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
