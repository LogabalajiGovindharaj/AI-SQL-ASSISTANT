package com.aisql.assistant.controller;

import com.aisql.assistant.model.Role;
import com.aisql.assistant.model.User;
import com.aisql.assistant.repository.UserRepository;
import com.aisql.assistant.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * These tests exercise the real Spring Security filter chain end-to-end -
 * they intentionally do NOT mock security, because the thing being verified
 * is that auth/role enforcement actually happens, not just that the
 * controller logic is correct.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User("user@test.com", "Test User", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(user);
        userToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        User admin = new User("admin@test.com", "Test Admin", passwordEncoder.encode("password123"), Role.ADMIN);
        userRepository.save(admin);
        adminToken = jwtUtil.generateToken(admin.getEmail(), admin.getRole().name());
    }

    @Test
    void loginAndSignupAreAccessibleWithoutAToken() throws Exception {
        // Bad credentials should yield a 400 from AuthService, never a 401/403 from the security filter.
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@test.com\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateQueryWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(post("/generate-query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Show all students\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void historyWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/history")).andExpect(status().isUnauthorized());
    }

    @Test
    void analyticsWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/analytics")).andExpect(status().isUnauthorized());
    }

    @Test
    void historyWithValidTokenIsAllowed() throws Exception {
        mockMvc.perform(get("/history").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void adminRouteRejectsRegularUser() throws Exception {
        mockMvc.perform(get("/admin/users").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRouteAllowsAdmin() throws Exception {
        mockMvc.perform(get("/admin/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void healthEndpointIsAccessibleWithoutAToken() throws Exception {
        mockMvc.perform(get("/health")).andExpect(status().isOk());
    }

    @Test
    void adminRouteRejectsGarbageToken() throws Exception {
        mockMvc.perform(get("/admin/users").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }
}
