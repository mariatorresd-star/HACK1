package com.example.hack1base.JWT.infraestructure;

import com.example.hack1base.JWT.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountRepositoryTest {

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
    @DisplayName("Debe guardar y retornar la entidad cuando se proporciona una cuenta válida")
    void shouldSaveAccountWhenValidDataProvided() {

        AccountRepository repo = mock(AccountRepository.class);
        Account in = mkAccount(null, "user@corp.com", "CENTRAL", "Miraflores", true);
        when(repo.save(in)).thenReturn(in);


        Account out = repo.save(in);


        verify(repo).save(in);
        assertSame(in, out);
    }

    @Test
    @DisplayName("Debe retornar todas las cuentas cuando existen registros")
    void shouldReturnAllAccountsWhenRecordsExist() {

        AccountRepository repo = mock(AccountRepository.class);
        List<Account> expected = List.of(
                mkAccount(1L, "a@corp.com", "CENTRAL", "Surco", true),
                mkAccount(2L, "b@corp.com", "BRANCH", "Miraflores", false)
        );
        when(repo.findAll()).thenReturn(expected);


        List<Account> out = repo.findAll();

        verify(repo).findAll();
        assertEquals(expected, out);
    }

    @Test
    @DisplayName("Debe retornar la cuenta cuando el ID existe en la base de datos")
    void shouldReturnAccountWhenIdExists() {

        AccountRepository repo = mock(AccountRepository.class);
        Account entity = mkAccount(10L, "x@corp.com", "CENTRAL", "Lince", true);
        when(repo.findById(10L)).thenReturn(Optional.of(entity));


        Optional<Account> out = repo.findById(10L);


        verify(repo).findById(10L);
        assertTrue(out.isPresent());
        assertSame(entity, out.get());
    }

    @Test
    @DisplayName("Debe retornar Optional con la cuenta cuando el correo existe")
    void shouldReturnOptionalWhenFindByEmailExists() {

        AccountRepository repo = mock(AccountRepository.class);
        String email = "login@corp.com";
        Account entity = mkAccount(3L, email, "BRANCH", "Surco", true);
        when(repo.findByEmail(email)).thenReturn(Optional.of(entity));


        Optional<Account> out = repo.findByEmail(email);


        verify(repo).findByEmail(email);
        assertTrue(out.isPresent());
        assertEquals(email, out.get().getEmail());
    }

    @Test
    @DisplayName("Debe retornar true cuando el correo ya está registrado")
    void shouldReturnTrueWhenEmailAlreadyExists() {

        AccountRepository repo = mock(AccountRepository.class);
        String email = "exists@corp.com";
        when(repo.existsByEmail(email)).thenReturn(true);


        boolean exists = repo.existsByEmail(email);


        verify(repo).existsByEmail(email);
        assertTrue(exists);
    }

    @Test
    @DisplayName("Debe eliminar la cuenta cuando se proporciona un ID válido")
    void shouldDeleteAccountWhenValidIdProvided() {

        AccountRepository repo = mock(AccountRepository.class);


        repo.deleteById(99L);


        verify(repo).deleteById(99L);
    }
}
