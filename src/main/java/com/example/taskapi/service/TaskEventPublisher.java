package com.example.taskapi.service;

import com.example.taskapi.dto.TaskCreatedEvent;

/**
 * Abstraction over "announce that a task was created".
 * <p>
 * When Kafka is enabled we publish to a topic; otherwise we log and move on.
 * Programming to an interface keeps {@link TaskService} testable with a Mockito mock
 * and means the service does not need to know about Kafka.
 */
public interface TaskEventPublisher {

    void publishTaskCreated(TaskCreatedEvent event);
}
