package com.example.taskapi.kafka;

import com.example.taskapi.dto.TaskCreatedEvent;
import com.example.taskapi.service.TaskEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer. {@link KafkaTemplate} is the helper Spring gives you for
 * sending messages — roughly a dedicated queue connection, like dispatching a
 * Laravel event/job to Redis or SQS, but to a Kafka topic.
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaTaskEventPublisher implements TaskEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaTaskEventPublisher.class);

    private final KafkaTemplate<String, TaskCreatedEvent> kafkaTemplate;
    private final String topic;

    public KafkaTaskEventPublisher(
            KafkaTemplate<String, TaskCreatedEvent> kafkaTemplate,
            @Value("${app.kafka.topic.task-created}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publishTaskCreated(TaskCreatedEvent event) {
        String key = String.valueOf(event.taskId());
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish TaskCreated for task {}: {}", event.taskId(), ex.getMessage());
                    } else {
                        log.info("Published TaskCreated for task {} to topic {}", event.taskId(), topic);
                    }
                });
    }
}
