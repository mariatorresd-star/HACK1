package com.example.hack1base.User.application;

import com.example.hack1base.User.domain.Role;
import com.example.hack1base.User.domain.User;
import com.example.hack1base.User.domain.UserResponse;
import com.example.hack1base.User.domain.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(UserControllerTest.MethodSecurityTestConfig.class)
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }

    // ---------- Helpers ----------
    private static User makeUserReq(String username, String email, String password, Role role, String branch) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(password);
        u.setRole(role);
        u.setBranch(branch);
        return u;
    }

    private static UserResponse makeResp(String id, String username, String email, String role, String branch) {
        UserResponse r = new UserResponse();
        r.setId(id);
        r.setUsername(username);
        r.setEmail(email);
        r.setRole(role);
        r.setBranch(branch);
        r.setCreatedAt(LocalDateTime.of(2025, 10, 31, 10, 0));
        return r;
    }

    // ---------- /auth/register ----------
    @Test
    @DisplayName("should register user and return 200 OK with UserResponse")
    void shouldRegisterReturnOkWithUserResponse() throws Exception {
        User req = makeUserReq("alice", "alice@example.com", "Secret123!", Role.CENTRAL, null);
        UserResponse resp = makeResp("1", "alice", "alice@example.com", "CENTRAL", null);

        Mockito.when(userService.register(org.mockito.ArgumentMatchers.<User>any()))
                .thenReturn(resp);

        mvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.role", is("CENTRAL")));

        org.mockito.Mockito.verify(userService)
                .register(org.mockito.ArgumentMatchers.<User>any());
    }

    // ---------- /auth/login ----------
    @Test
    @DisplayName("should return 200 OK when credentials are valid")
    void shouldLoginReturnOkWhenCredentialsAreValid() throws Exception {
        User req = new User();
        req.setEmail("alice@example.com");
        req.setPassword("Secret123!");

        UserResponse resp = makeResp("1", "alice", "alice@example.com", "CENTRAL", null);

        Mockito.when(userService.login(
                org.mockito.ArgumentMatchers.eq("alice@example.com"),
                org.mockito.ArgumentMatchers.eq("Secret123!")
        )).thenReturn(resp);

        mvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("alice")));
    }

    // ---------- Endpoints protegidos ----------
    @Nested
    @DisplayName("Security and access to /users*")
    class SecuredEndpoints {

        @Test
        @DisplayName("should return 401 Unauthorized on GET /users when unauthenticated")
        void shouldReturnUnauthorizedOnGetAllWhenUnauthenticated() throws Exception {
            mvc.perform(get("/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 Forbidden on GET /users for BRANCH role")
        @WithMockUser(roles = {"BRANCH"})
        void shouldReturnForbiddenOnGetAllForBranchRole() throws Exception {
            mvc.perform(get("/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 200 OK with mapped list on GET /users for CENTRAL role")
        @WithMockUser(roles = {"CENTRAL"})
        void shouldReturnOkWithListOnGetAllForCentralRole() throws Exception {
            List<UserResponse> list = List.of(
                    makeResp("1", "alice", "alice@example.com", "CENTRAL", null),
                    makeResp("2", "bob", "bob@example.com", "BRANCH", "LIMA-01")
            );
            Mockito.when(userService.getAllUsers()).thenReturn(list);

            mvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].username", is("alice")))
                    .andExpect(jsonPath("$[1].role", is("BRANCH")));
        }

        @Test
        @DisplayName("should return 401 Unauthorized on GET /users/{id} when unauthenticated")
        void shouldReturnUnauthorizedOnGetByIdWhenUnauthenticated() throws Exception {
            mvc.perform(get("/users/9"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 Forbidden on GET /users/{id} for BRANCH role")
        @WithMockUser(roles = {"BRANCH"})
        void shouldReturnForbiddenOnGetByIdForBranchRole() throws Exception {
            mvc.perform(get("/users/9"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 200 OK with body on GET /users/{id} for CENTRAL role")
        @WithMockUser(roles = {"CENTRAL"})
        void shouldReturnOkOnGetByIdForCentralRole() throws Exception {
            UserResponse resp = makeResp("9", "carol", "carol@example.com", "BRANCH", "SURCO-01");
            Mockito.when(userService.getUserById(9L)).thenReturn(resp);

            mvc.perform(get("/users/9"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is("9")))
                    .andExpect(jsonPath("$.username", is("carol")))
                    .andExpect(jsonPath("$.branch", is("SURCO-01")));
        }

        @Test
        @DisplayName("should return 401 Unauthorized on DELETE /users/{id} when unauthenticated")
        void shouldReturnUnauthorizedOnDeleteWhenUnauthenticated() throws Exception {
            mvc.perform(delete("/users/7"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 Forbidden on DELETE /users/{id} for BRANCH role")
        @WithMockUser(roles = {"BRANCH"})
        void shouldReturnForbiddenOnDeleteForBranchRole() throws Exception {
            mvc.perform(delete("/users/7"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 204 No Content on DELETE /users/{id} for CENTRAL role")
        @WithMockUser(roles = {"CENTRAL"})
        void shouldReturnNoContentOnDeleteForCentralRole() throws Exception {
            mvc.perform(delete("/users/7"))
                    .andExpect(status().isNoContent());

            org.mockito.Mockito.verify(userService).deleteUser(7L);
        }
    }
}
