package com.example.taskapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test against a real PostgreSQL started by Testcontainers.
 * <p>
 * If Docker is not available the class is skipped, not failed
 * ({@code @EnabledIf} plus {@code disabledWithoutDocker}).
 * Run it on your machine with Docker Desktop (or equivalent) started:
 * {@code ./mvnw test -Dtest=TaskApiIntegrationTest}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@EnabledIf(value = "dockerAvailable", disabledReason = "Docker is not available; Testcontainers integration test skipped")
class TaskApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Cheap check first (socket / DOCKER_HOST) so we do not hang waiting for a Docker client
     * when Docker is not installed at all.
     */
    static boolean dockerAvailable() {
        try {
            Path socket = Path.of("/var/run/docker.sock");
            String dockerHost = System.getenv("DOCKER_HOST");
            if (!Files.exists(socket) && (dockerHost == null || dockerHost.isBlank())) {
                return false;
            }
            return org.testcontainers.DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable ex) {
            return false;
        }
    }

    @Test
    void registerLoginAndCrudTasksAgainstPostgres() throws Exception {
        String token = registerAndGetToken("alice@example.com", "Alice");

        mockMvc.perform(get("/api/tasks").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        MvcResult created = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Learn Spring Boot","description":"Build the starter API"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andReturn();

        long taskId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/tasks/" + taskId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId));

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Learn Spring Boot","description":"Halfway","status":"IN_PROGRESS"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.description").value("Halfway"));

        String otherToken = registerAndGetToken("bob@example.com", "Bob");
        mockMvc.perform(get("/api/tasks/" + taskId).header("Authorization", bearer(otherToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/tasks").header("Authorization", bearer(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(delete("/api/tasks/" + taskId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/" + taskId).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void protectedRoutesRequireAJwt() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsTheWrongPassword() throws Exception {
        registerAndGetToken("carol@example.com", "Carol");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"carol@example.com","password":"this-is-wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String registerAndGetToken(String email, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"secret123","name":"%s"}
                                """.formatted(email, name)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
