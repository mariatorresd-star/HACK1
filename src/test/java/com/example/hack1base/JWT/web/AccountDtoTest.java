package com.example.hack1base.JWT.web;

import com.example.hack1base.JWT.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AccountDtoTest {


    private Account mkAccount(Long id, String email, String role, String branch, Instant createdAt) {
        Account a = new Account();
        a.setId(id);
        a.setEmail(email);
        a.setPassword("secret");
        a.setRole(role);
        a.setBranch(branch);
        a.setCreatedAt(createdAt);
        return a;
    }

    @Test
    @DisplayName("shouldMapAllFieldsWhenAccountHasValues")
    void shouldMapAllFieldsWhenAccountHasValues() {

        Instant created = Instant.parse("2025-01-10T12:34:56Z");
        Account account = mkAccount(7L, "user@corp.com", "CENTRAL", "Miraflores", created);


        AccountDto dto = AccountDto.from(account);


        assertAll(
                () -> assertEquals(7L, dto.getId()),
                () -> assertEquals("user@corp.com", dto.getEmail()),
                () -> assertEquals("CENTRAL", dto.getRole()),
                () -> assertEquals("Miraflores", dto.getBranch()),
                // createdAt en DTO es String con Instant.toString()
                () -> assertEquals(created.toString(), dto.getCreatedAt())
        );
    }

    @Test
    @DisplayName("shouldThrowNullPointerWhenCreatedAtIsNull")
    void shouldThrowNullPointerWhenCreatedAtIsNull() {

        Account account = mkAccount(1L, "x@corp.com", "BRANCH", "Surco", null);


        assertThrows(NullPointerException.class, () -> AccountDto.from(account),
                "from() invoca toString() sobre createdAt y debe lanzar NPE si es null");
    }

    @Test
    @DisplayName("shouldAllowNullRoleAndBranchWhenTheyAreNull")
    void shouldAllowNullRoleAndBranchWhenTheyAreNull() {

        Instant created = Instant.parse("2025-02-01T00:00:00Z");
        Account account = mkAccount(9L, "nulls@corp.com", null, null, created);


        AccountDto dto = AccountDto.from(account);


        assertAll(
                () -> assertEquals(9L, dto.getId()),
                () -> assertEquals("nulls@corp.com", dto.getEmail()),
                () -> assertNull(dto.getRole()),
                () -> assertNull(dto.getBranch()),
                () -> assertEquals(created.toString(), dto.getCreatedAt())
        );
    }
}
