package com.example.taskapi.service;

import com.example.taskapi.dto.TaskCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Fallback publisher used when Kafka is disabled (the default, and all tests).
 * The matching Kafka implementation is {@code KafkaTaskEventPublisher},
 * registered only when {@code app.kafka.enabled=true}.
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpTaskEventPublisher implements TaskEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpTaskEventPublisher.class);

    @Override
    public void publishTaskCreated(TaskCreatedEvent event) {
        log.debug("Kafka disabled; skipping TaskCreated event for task {}", event.taskId());
    }
}
