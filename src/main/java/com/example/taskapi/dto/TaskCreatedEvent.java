package com.example.taskapi.dto;

import java.time.Instant;

/**
 * Payload published to Kafka when a task is created.
 * Keep events as plain data (records work well) so producers and consumers stay decoupled.
 */
public record TaskCreatedEvent(Long taskId, String title, Long ownerId, Instant createdAt) {
}
