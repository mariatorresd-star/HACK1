package com.example.hack1base.JWT.web;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    @DisplayName("shouldExposeAllFieldsWhenCreatedWithAllArgsConstructor")
    void shouldExposeAllFieldsWhenCreatedWithAllArgsConstructor() {

        String token = "abc.def.ghi";
        long expiresAt = 1735689600000L;
        String role = "CENTRAL";
        String branch = "Miraflores";


        AuthResponse resp = new AuthResponse(token, expiresAt, role, branch);


        assertAll(
                () -> assertEquals(token, resp.getToken()),
                () -> assertEquals(expiresAt, resp.getExpiresAtMillis()),
                () -> assertEquals(role, resp.getRole()),
                () -> assertEquals(branch, resp.getBranch())
        );
    }

    @Test
    @DisplayName("shouldAllowBoundaryValuesWhenConstructed")
    void shouldAllowBoundaryValuesWhenConstructed() {

        String token = "";
        long expiresAt = 0L;
        String role = null;
        String branch = null;


        AuthResponse resp = new AuthResponse(token, expiresAt, role, branch);


        assertAll(
                () -> assertEquals("", resp.getToken()),
                () -> assertEquals(0L, resp.getExpiresAtMillis()),
                () -> assertNull(resp.getRole()),
                () -> assertNull(resp.getBranch())
        );
    }

    @Test
    @DisplayName("shouldBeImmutableWhenTryingToChangeFields")
    void shouldBeImmutableWhenTryingToChangeFields() {

        AuthResponse resp = new AuthResponse("tkn", 1L, "BRANCH", "Surco");


        assertAll(
                () -> assertDoesNotThrow(resp::getToken),
                () -> assertDoesNotThrow(resp::getExpiresAtMillis),
                () -> assertDoesNotThrow(resp::getRole),
                () -> assertDoesNotThrow(resp::getBranch)
        );
    }
}
