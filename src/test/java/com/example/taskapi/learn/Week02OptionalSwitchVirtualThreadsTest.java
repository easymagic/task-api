package com.example.taskapi.learn;

import com.example.taskapi.domain.TaskStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Week 2 exercises: {@link Optional}, switch expressions and virtual threads.
 * In the app, repositories return {@code Optional<Task>}, and
 * {@code spring.threads.virtual.enabled=true} in {@code application.yml}
 * asks Tomcat to run requests on virtual threads (Java 21).
 */
class Week02OptionalSwitchVirtualThreadsTest {

    @Test
    void optionalReplacesNullChecks() {
        Optional<String> present = Optional.of("Learn Java 21");
        Optional<String> missing = Optional.empty();

        String title = present.orElse("untitled");
        String fallback = missing.orElse("untitled");
        Optional<Integer> length = present.map(String::length);

        assertThat(title).isEqualTo("Learn Java 21");
        assertThat(fallback).isEqualTo("untitled");
        assertThat(length).contains(13);
        assertThat(missing.isPresent()).isFalse();
    }

    @Test
    void switchExpressionsCanReturnAValue() {
        assertThat(labelFor(TaskStatus.TODO)).isEqualTo("Ready to start");
        assertThat(labelFor(TaskStatus.IN_PROGRESS)).isEqualTo("Being worked on");
        assertThat(labelFor(TaskStatus.DONE)).isEqualTo("Finished");
    }

    @Test
    void virtualThreadsCanRunManyTasksCheaply() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> one = executor.submit(counter::incrementAndGet);
            Future<?> two = executor.submit(counter::incrementAndGet);
            one.get();
            two.get();
        }
        assertThat(counter.get()).isEqualTo(2);
        assertThat(Thread.ofVirtual().unstarted(() -> {}).isVirtual()).isTrue();
    }

    /**
     * Switch expressions must be exhaustive. Add a new enum constant and
     * this method will fail to compile until you add a case — a feature
     * older {@code switch} statements did not give you.
     */
    private static String labelFor(TaskStatus status) {
        return switch (status) {
            case TODO -> "Ready to start";
            case IN_PROGRESS -> "Being worked on";
            case DONE -> "Finished";
        };
    }
}
