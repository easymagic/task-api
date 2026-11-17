package com.example.taskapi.service;

import com.example.taskapi.domain.User;
import com.example.taskapi.dto.AuthResponse;
import com.example.taskapi.dto.LoginRequest;
import com.example.taskapi.dto.RegisterRequest;
import com.example.taskapi.exception.DuplicateEmailException;
import com.example.taskapi.repository.UserRepository;
import com.example.taskapi.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Register and login. Analogous to a Laravel {@code AuthController} +
 * {@code Hash::make} + issuing a Sanctum token, all in one service.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
        User user = new User(email, passwordEncoder.encode(request.password()), request.name().trim());
        users.save(user);
        log.info("Registered user id={} email={}", user.getId(), user.getEmail());
        return new AuthResponse(jwtService.createToken(user.getEmail()), user.getEmail(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }
        User user = users.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return new AuthResponse(jwtService.createToken(user.getEmail()), user.getEmail(), user.getName());
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
