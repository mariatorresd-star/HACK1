package com.example.hack1base.JWT.domain;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    @DisplayName("NoArgsConstructor + setters: asigna y recupera campos correctamente")
    void shouldAssignFieldsWithSetters() {

        Account acc = new Account();
        Instant created = Instant.parse("2025-01-01T10:15:30Z");


        acc.setId(1L);
        acc.setEmail("user@example.com");
        acc.setPassword("secret");
        acc.setRole("CENTRAL");
        acc.setBranch("Miraflores");
        acc.setEnabled(false);
        acc.setCreatedAt(created);


        assertAll(
                () -> assertEquals(1L, acc.getId()),
                () -> assertEquals("user@example.com", acc.getEmail()),
                () -> assertEquals("secret", acc.getPassword()),
                () -> assertEquals("CENTRAL", acc.getRole()),
                () -> assertEquals("Miraflores", acc.getBranch()),
                () -> assertFalse(acc.isEnabled()),
                () -> assertEquals(created, acc.getCreatedAt())
        );
    }

    @Test
    @DisplayName("Valores por defecto: enabled=true y createdAt cercano a ahora")
    void shouldHaveSensibleDefaults() {

        Instant before = Instant.now().minusSeconds(3);


        Account acc = new Account();


        assertTrue(acc.isEnabled(), "enabled por defecto debe ser true");
        assertNotNull(acc.getCreatedAt(), "createdAt no debe ser null");
        Instant after = Instant.now().plusSeconds(3);
        assertTrue(!acc.getCreatedAt().isBefore(before) && !acc.getCreatedAt().isAfter(after),
                "createdAt debe ser cercano a ahora");
    }

    @Test
    @DisplayName("getUsername retorna email; banderas UserDetails son true salvo enabled")
    void shouldExposeUserDetailsFlags() {

        Account acc = new Account();
        acc.setEmail("admin@corp.com");
        acc.setEnabled(true);


        assertEquals("admin@corp.com", acc.getUsername());
        assertTrue(acc.isAccountNonExpired());
        assertTrue(acc.isAccountNonLocked());
        assertTrue(acc.isCredentialsNonExpired());
        assertTrue(acc.isEnabled());
    }

    @Test
    @DisplayName("getAuthorities: agrega prefijo ROLE_ al valor de role")
    void shouldExposeAuthorityWithRolePrefix() {

        Account acc = new Account();
        acc.setRole("BRANCH");


        Collection<? extends GrantedAuthority> authorities = acc.getAuthorities();


        assertEquals(1, authorities.size(), "Debe haber exactamente una autoridad");
        GrantedAuthority auth = authorities.iterator().next();
        assertEquals("ROLE_BRANCH", auth.getAuthority());
    }

    @Test
    @DisplayName("getAuthorities: maneja role null generando ROLE_null (comportamiento actual)")
    void shouldHandleNullRoleCurrently() {

        Account acc = new Account();

        GrantedAuthority auth = acc.getAuthorities().iterator().next();

        assertEquals("ROLE_null", auth.getAuthority(),
                "Con role=null, el comportamiento actual produce ROLE_null");
    }
}

