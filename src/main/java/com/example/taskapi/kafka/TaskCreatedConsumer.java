package com.example.taskapi.kafka;

import com.example.taskapi.dto.TaskCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer. {@code @KafkaListener} is the subscriber — like a Laravel
 * event listener, except the message may have been produced by this app or
 * by another service sharing the topic.
 * <p>
 * This starter just logs the event. In a real system you might send email,
 * update a search index, or push a notification.
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class TaskCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(TaskCreatedConsumer.class);

    @KafkaListener(topics = "${app.kafka.topic.task-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void onTaskCreated(TaskCreatedEvent event) {
        log.info("Consumed TaskCreated event: taskId={}, title={}, ownerId={}",
                event.taskId(), event.title(), event.ownerId());
    }
}
