package com.example.taskapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the application.
 * <p>
 * {@code @SpringBootApplication} turns on component scanning and auto-configuration.
 * Think of this as {@code public/index.php} plus the service providers Laravel
 * would register in {@code bootstrap/app.php}: Spring looks for
 * {@code @Component}, {@code @Service}, {@code @Repository} and {@code @RestController}
 * under this package and wires them into the application context (the DI container).
 */
@SpringBootApplication
public class TaskApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskApiApplication.class, args);
    }
}
