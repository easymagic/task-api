package com.example.taskapi.learn;

import com.example.taskapi.domain.TaskStatus;
import com.example.taskapi.dto.TaskResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Week 1 exercises: records, {@code var}, streams and collections.
 * These tests already pass — break them on purpose, then fix them, to feel
 * how modern Java reads compared with older Android Java.
 * <p>
 * The production DTOs in {@code com.example.taskapi.dto} are records too;
 * {@link com.example.taskapi.service.TaskService#listMyTasks()} uses a stream.
 */
class Week01RecordsStreamsCollectionsTest {

    private final Instant now = Instant.parse("2026-01-15T10:00:00Z");

    @Test
    void recordsGiveYouAccessorsEqualsAndToString() {
        TaskResponse left = sample(1L, "Write tests", TaskStatus.TODO);
        TaskResponse right = sample(1L, "Write tests", TaskStatus.TODO);

        assertThat(left.title()).isEqualTo("Write tests");
        assertThat(left).isEqualTo(right);
        assertThat(left.toString()).contains("Write tests");
    }

    @Test
    void varIsJustTypeInferenceForLocalVariables() {
        var title = "Write tests";
        var ids = List.of(1L, 2L, 3L);

        assertThat(title).isInstanceOf(String.class);
        assertThat(ids).hasSize(3);
    }

    @Test
    void streamsFilterMapAndCollectLikeCollectionPipelines() {
        var tasks = List.of(
                sample(1L, "Write tests", TaskStatus.TODO),
                sample(2L, "Read docs", TaskStatus.DONE),
                sample(3L, "Ship it", TaskStatus.TODO)
        );

        var openTitles = tasks.stream()
                .filter(task -> task.status() == TaskStatus.TODO)
                .map(TaskResponse::title)
                .sorted()
                .toList();

        assertThat(openTitles).containsExactly("Ship it", "Write tests");
    }

    @Test
    void collectionsIncludeListMapAndGrouping() {
        var tasks = List.of(
                sample(1L, "A", TaskStatus.TODO),
                sample(2L, "B", TaskStatus.DONE),
                sample(3L, "C", TaskStatus.TODO)
        );

        Map<TaskStatus, Long> counts = tasks.stream()
                .collect(Collectors.groupingBy(TaskResponse::status, Collectors.counting()));

        var sortedByTitle = tasks.stream()
                .sorted(Comparator.comparing(TaskResponse::title))
                .map(TaskResponse::title)
                .toList();

        assertThat(counts.get(TaskStatus.TODO)).isEqualTo(2L);
        assertThat(sortedByTitle).containsExactly("A", "B", "C");
    }

    private TaskResponse sample(Long id, String title, TaskStatus status) {
        return new TaskResponse(id, title, null, status, 10L, now, now);
    }
}
