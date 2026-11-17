package com.example.taskapi.dto;

import com.example.taskapi.domain.Task;
import com.example.taskapi.domain.TaskStatus;

import java.time.Instant;

/** Outgoing JSON for a task. Built with a static factory so the controller stays thin. */
public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Long ownerId,
        Instant createdAt,
        Instant updatedAt
) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getOwner().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
