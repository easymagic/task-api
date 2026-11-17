package com.example.taskapi.service;

import com.example.taskapi.domain.User;
import com.example.taskapi.exception.ResourceNotFoundException;
import com.example.taskapi.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resolves the signed-in user from Spring Security's context.
 * {@code SecurityContextHolder.getContext().getAuthentication().getName()} is
 * the email we put in the JWT subject — similar to {@code auth()->user()}.
 */
@Service
public class CurrentUserService {

    private final UserRepository users;

    public CurrentUserService(UserRepository users) {
        this.users = users;
    }

    public User require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResourceNotFoundException("No authenticated user");
        }
        return users.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", authentication.getName()));
    }
}
