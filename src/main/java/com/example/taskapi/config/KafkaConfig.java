package com.example.taskapi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Turns on {@code @KafkaListener} detection only when {@code app.kafka.enabled=true}.
 * Weeks 3–4 can run the API against Postgres alone; week 5 flips the flag.
 */
@Configuration
@EnableKafka
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaConfig {
}
