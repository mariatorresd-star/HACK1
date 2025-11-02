package com.example.hack1base.JWT.services;


import com.example.hack1base.JWT.domain.Account;
import com.example.hack1base.JWT.infraestructure.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository repo;

    @InjectMocks
    private AccountService service;

    // -------- helper --------
    private Account mkAccount(Long id, String email, String role, String branch, boolean enabled) {
        Account a = new Account();
        a.setId(id);
        a.setEmail(email);
        a.setPassword("secret");
        a.setRole(role);
        a.setBranch(branch);
        a.setEnabled(enabled);
        a.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        return a;
    }

    @Test
    @DisplayName("loadUserByUsername: debe retornar UserDetails cuando el email existe")
    void shouldReturnUserDetailsWhenEmailExists() {

        String email = "user@corp.com";
        Account acc = mkAccount(1L, email, "CENTRAL", "Miraflores", true);
        when(repo.findByEmail(email)).thenReturn(Optional.of(acc));


        var user = service.loadUserByUsername(email);


        verify(repo).findByEmail(email);
        assertNotNull(user);
        assertEquals(email, user.getUsername());
        assertTrue(user.isEnabled());
    }

    @Test
    @DisplayName("loadUserByUsername: debe lanzar UsernameNotFoundException cuando el email no existe")
    void shouldThrowExceptionWhenEmailNotExists() {

        String email = "missing@corp.com";
        when(repo.findByEmail(email)).thenReturn(Optional.empty());

               UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(email)
        );
        assertTrue(ex.getMessage().contains(email));
        verify(repo).findByEmail(email);
    }

    @Test
    @DisplayName("findByEmailOpt: debe retornar Optional con cuenta cuando existe")
    void shouldReturnOptionalWhenFindByEmailExists() {

        String email = "exists@corp.com";
        Account acc = mkAccount(2L, email, "BRANCH", "Surco", true);
        when(repo.findByEmail(email)).thenReturn(Optional.of(acc));


        Optional<Account> out = service.findByEmailOpt(email);


        verify(repo).findByEmail(email);
        assertTrue(out.isPresent());
        assertEquals(email, out.get().getEmail());
    }

    @Test
    @DisplayName("findByEmailOpt: debe retornar Optional vacío cuando NO existe")
    void shouldReturnEmptyOptionalWhenFindByEmailNotExists() {

        String email = "none@corp.com";
        when(repo.findByEmail(email)).thenReturn(Optional.empty());


        Optional<Account> out = service.findByEmailOpt(email);


        verify(repo).findByEmail(email);
        assertTrue(out.isEmpty());
    }

    @Test
    @DisplayName("save: debe delegar en repo.save y retornar la cuenta persistida")
    void shouldSaveAccountWhenValidDataProvided() {

        Account in = mkAccount(null, "new@corp.com", "CENTRAL", "Lince", true);
        when(repo.save(in)).thenReturn(in);


        Account out = service.save(in);


        verify(repo).save(in);
        assertSame(in, out);
    }
}
