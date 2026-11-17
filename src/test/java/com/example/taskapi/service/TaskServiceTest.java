package com.example.taskapi.service;

import com.example.taskapi.domain.Task;
import com.example.taskapi.domain.TaskStatus;
import com.example.taskapi.domain.User;
import com.example.taskapi.dto.TaskCreatedEvent;
import com.example.taskapi.dto.TaskRequest;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.exception.ResourceNotFoundException;
import com.example.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TaskService}. Mockito stands in for the database,
 * the current user, and Kafka — no Spring context is started.
 * This is the same idea as a PHPUnit test that mocks Eloquent and Auth.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository tasks;

    @Mock
    private CurrentUserService currentUser;

    @Mock
    private TaskEventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User("alice@example.com", "hash", "Alice");
        owner.setId(10L);
        when(currentUser.require()).thenReturn(owner);
    }

    @Test
    void listMyTasksReturnsOnlyTheCurrentUsersTasks() {
        Task first = persistedTask(1L, "Write tests", TaskStatus.TODO);
        Task second = persistedTask(2L, "Read docs", TaskStatus.DONE);
        when(tasks.findByOwnerIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(first, second));

        List<TaskResponse> result = taskService.listMyTasks();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TaskResponse::title).containsExactly("Write tests", "Read docs");
        assertThat(result).allMatch(response -> response.ownerId().equals(10L));
    }

    @Test
    void getThrowsWhenTaskIsMissingOrNotOwned() {
        when(tasks.findByIdAndOwnerId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createPersistsATodoAndPublishesAnEvent() {
        when(tasks.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(42L);
            return task;
        });

        TaskRequest request = new TaskRequest("Ship the API", "First cut", null);
        TaskResponse response = taskService.create(request);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.title()).isEqualTo("Ship the API");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.ownerId()).isEqualTo(10L);

        ArgumentCaptor<TaskCreatedEvent> event = ArgumentCaptor.forClass(TaskCreatedEvent.class);
        verify(eventPublisher).publishTaskCreated(event.capture());
        assertThat(event.getValue().taskId()).isEqualTo(42L);
        assertThat(event.getValue().title()).isEqualTo("Ship the API");
        assertThat(event.getValue().ownerId()).isEqualTo(10L);
    }

    @Test
    void updateChangesFieldsOnAnOwnedTask() {
        Task existing = persistedTask(5L, "Old title", TaskStatus.TODO);
        when(tasks.findByIdAndOwnerId(5L, 10L)).thenReturn(Optional.of(existing));

        TaskRequest request = new TaskRequest("New title", "Updated", TaskStatus.IN_PROGRESS);
        TaskResponse response = taskService.update(5L, request);

        assertThat(response.title()).isEqualTo("New title");
        assertThat(response.description()).isEqualTo("Updated");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(existing.getTitle()).isEqualTo("New title");
    }

    @Test
    void deleteRemovesAnOwnedTask() {
        Task existing = persistedTask(7L, "Remove me", TaskStatus.TODO);
        when(tasks.findByIdAndOwnerId(7L, 10L)).thenReturn(Optional.of(existing));

        taskService.delete(7L);

        verify(tasks).delete(existing);
    }

    @Test
    void deleteDoesNotTouchTheRepositoryWhenTheTaskIsNotOwned() {
        when(tasks.findByIdAndOwnerId(7L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(7L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(tasks, never()).delete(any());
    }

    private Task persistedTask(Long id, String title, TaskStatus status) {
        Task task = new Task(title, "notes", status, owner);
        task.setId(id);
        return task;
    }
}
