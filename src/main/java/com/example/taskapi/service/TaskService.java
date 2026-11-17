package com.example.taskapi.service;

import com.example.taskapi.domain.Task;
import com.example.taskapi.domain.TaskStatus;
import com.example.taskapi.domain.User;
import com.example.taskapi.dto.TaskCreatedEvent;
import com.example.taskapi.dto.TaskRequest;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.exception.ResourceNotFoundException;
import com.example.taskapi.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application logic for tasks. In Laravel this would often live in a service
 * class or even on the controller; Spring convention is a {@code @Service}
 * that the controller delegates to, which also makes unit testing easy
 * (see {@code TaskServiceTest}).
 * <p>
 * {@code @Transactional} starts a database transaction (like {@code DB::transaction}).
 * Read-only methods can skip the write lock with {@code readOnly = true}.
 */
@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository tasks;
    private final CurrentUserService currentUser;
    private final TaskEventPublisher eventPublisher;

    public TaskService(TaskRepository tasks, CurrentUserService currentUser, TaskEventPublisher eventPublisher) {
        this.tasks = tasks;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listMyTasks() {
        User owner = currentUser.require();
        return tasks.findByOwnerIdOrderByCreatedAtDesc(owner.getId()).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long id) {
        return TaskResponse.from(requireOwnedTask(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        User owner = currentUser.require();
        TaskStatus status = request.status() == null ? TaskStatus.TODO : request.status();
        Task task = new Task(request.title().trim(), trimToNull(request.description()), status, owner);
        tasks.save(task);
        log.info("Created task id={} for user {}", task.getId(), owner.getEmail());
        eventPublisher.publishTaskCreated(
                new TaskCreatedEvent(task.getId(), task.getTitle(), owner.getId(), task.getCreatedAt()));
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = requireOwnedTask(id);
        task.setTitle(request.title().trim());
        task.setDescription(trimToNull(request.description()));
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        // No explicit save needed: the persistence context flushes dirty entities at commit.
        log.info("Updated task id={}", task.getId());
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(Long id) {
        Task task = requireOwnedTask(id);
        tasks.delete(task);
        log.info("Deleted task id={}", id);
    }

    private Task requireOwnedTask(Long id) {
        User owner = currentUser.require();
        return tasks.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
