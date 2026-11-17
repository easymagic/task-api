package com.example.taskapi.repository;

import com.example.taskapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository ≈ an Eloquent model plus a query scope.
 * <p>
 * You do not write an implementation. Spring generates one from the method names:
 * {@code findByEmail} becomes {@code SELECT ... FROM users WHERE email = ?}.
 * {@code JpaRepository} already gives you {@code save}, {@code findById}, {@code delete}, etc.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
