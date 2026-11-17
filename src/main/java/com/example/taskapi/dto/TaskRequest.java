package com.example.taskapi.dto;

import com.example.taskapi.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Incoming JSON for creating or updating a task. Status may be omitted on create (defaults to TODO). */
public record TaskRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        TaskStatus status
) {
}
