package com.example.hack1base.JWT.appplication;


import com.example.hack1base.JWT.application.AuthController;
import com.example.hack1base.JWT.domain.Account;
import com.example.hack1base.JWT.services.AuthService;
import com.example.hack1base.JWT.web.AuthResponse;
import com.example.hack1base.JWT.web.LoginRequest;
import com.example.hack1base.JWT.web.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockitoBean
    private AuthService authService;

    // ===== Helpers =====
    private Account mkAccount(Long id, String email, String role, String branch, Instant createdAt) {
        Account a = new Account();
        a.setId(id);
        a.setEmail(email);
        a.setPassword("encoded");
        a.setRole(role);
        a.setBranch(branch);
        a.setCreatedAt(createdAt);
        return a;
    }

    // ============ REGISTER ============
    @Test
    @DisplayName("shouldReturn201AndAccountDtoWhenRegisterIsSuccessful")
    void shouldReturn201AndAccountDtoWhenRegisterIsSuccessful() throws Exception {

        var req = new RegisterRequest();
        req.setEmail("new@corp.com");
        req.setPassword("12345678");
        req.setRole("CENTRAL");
        req.setBranch("Surco");

        var created = mkAccount(10L, "new@corp.com", "CENTRAL", "Surco",
                Instant.parse("2025-01-01T00:00:00Z"));

        when(authService.register(any(RegisterRequest.class))).thenReturn(created);

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.email", is("new@corp.com")))
                .andExpect(jsonPath("$.role", is("CENTRAL")))
                .andExpect(jsonPath("$.branch", is("Surco")))
                .andExpect(jsonPath("$.createdAt", is("2025-01-01T00:00:00Z")));
    }

    @Test
    @DisplayName("shouldReturn400WhenRegisterRequestIsInvalid")
    void shouldReturn400WhenRegisterRequestIsInvalid() throws Exception {

        var req = new RegisterRequest();
        req.setEmail("bad-email");
        req.setPassword("short");
        req.setRole("  "); // blank

        // Act & Assert
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ============== LOGIN ==============
    @Test
    @DisplayName("shouldReturn200AndAuthResponseWhenLoginIsSuccessful")
    void shouldReturn200AndAuthResponseWhenLoginIsSuccessful() throws Exception {

        var req = new LoginRequest();
        req.setEmail("user@corp.com");
        req.setPassword("secretPass");

        var resp = new AuthResponse(
                "jwt.token.here",
                1735689600000L,
                "CENTRAL",
                "Miraflores"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(resp);


        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is("jwt.token.here")))
                .andExpect(jsonPath("$.expiresAtMillis", is(1735689600000L), Long.class))
                .andExpect(jsonPath("$.role", is("CENTRAL")))
                .andExpect(jsonPath("$.branch", is("Miraflores")));
    }

    @Test
    @DisplayName("shouldReturn400WhenLoginRequestIsInvalid")
    void shouldReturn400WhenLoginRequestIsInvalid() throws Exception {

        var req = new LoginRequest();
        req.setEmail("   ");
        req.setPassword("   ");


        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}

