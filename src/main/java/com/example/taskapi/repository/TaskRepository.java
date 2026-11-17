package com.example.taskapi.repository;

import com.example.taskapi.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Task queries. {@code findByIdAndOwnerId} is how we scope a row to its owner
 * without loading someone else's task — similar to {@code Task::where('user_id', $id)}.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);
}
