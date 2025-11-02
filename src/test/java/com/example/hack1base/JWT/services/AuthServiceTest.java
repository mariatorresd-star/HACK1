package com.example.hack1base.JWT.services;

import com.example.hack1base.Exceptions.ConflictException;
import com.example.hack1base.JWT.domain.Account;
import com.example.hack1base.JWT.security.JwtService;
import com.example.hack1base.JWT.web.AuthResponse;
import com.example.hack1base.JWT.web.LoginRequest;
import com.example.hack1base.JWT.web.RegisterRequest;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authManager;
    @Mock private AccountService accountService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    // ------- helper -------
    private Account mkAccount(String email, String role, String branch) {
        Account acc = new Account();
        acc.setEmail(email);
        acc.setPassword("encoded");
        acc.setRole(role);
        acc.setBranch(branch);
        return acc;
    }

    // ===================== LOGIN =====================

    @Test
    @DisplayName("shouldReturnAuthResponseWhenCredentialsAreValid")
    void shouldReturnAuthResponseWhenCredentialsAreValid() {

        LoginRequest req = new LoginRequest();
        req.setEmail("user@corp.com");
        req.setPassword("secret");

        Account account = mkAccount(req.getEmail(), "CENTRAL", "Miraflores");
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(account);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken(account.getEmail(), account.getRole(), account.getBranch()))
                .thenReturn("token123");
        when(jwtService.isTokenExpired("token123")).thenReturn(false);

        Claims claims = mock(Claims.class);
        when(jwtService.extractAllClaims("token123")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 10_000));


        AuthResponse response = authService.login(req);


        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(account.getEmail(), account.getRole(), account.getBranch());
        assertNotNull(response);
        assertEquals("token123", response.getToken());
        assertEquals("CENTRAL",  response.getRole());
        assertEquals("Miraflores", response.getBranch());
    }

    // ===================== REGISTER =====================

    @Test
    @DisplayName("shouldRegisterAccountWhenValidDataProvided")
    void shouldRegisterAccountWhenValidDataProvided() {
        // Arrange
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@corp.com");
        req.setPassword("1234");
        req.setRole("CENTRAL");
        req.setBranch("Surco");

        when(accountService.findByEmailOpt(req.getEmail())).thenReturn(Optional.empty());
        when(req.isRoleValid()).thenReturn(true);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");

        Account expected = mkAccount(req.getEmail(), req.getRole(), req.getBranch());
        when(accountService.save(any(Account.class))).thenReturn(expected);


        Account result = authService.register(req);


        verify(accountService).findByEmailOpt(req.getEmail());
        verify(accountService).save(any(Account.class));
        assertEquals("new@corp.com", result.getEmail());
        assertEquals("CENTRAL", result.getRole());
        assertEquals("Surco", result.getBranch());
    }

    @Test
    @DisplayName("shouldThrowConflictExceptionWhenEmailAlreadyExists")
    void shouldThrowConflictExceptionWhenEmailAlreadyExists() {

        RegisterRequest req = new RegisterRequest();
        req.setEmail("dup@corp.com");
        req.setPassword("123");
        req.setRole("CENTRAL");
        req.setBranch("Surco");

        when(accountService.findByEmailOpt(req.getEmail())).thenReturn(Optional.of(new Account()));


        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(accountService).findByEmailOpt(req.getEmail());
        verify(accountService, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentWhenRoleIsInvalid")
    void shouldThrowIllegalArgumentWhenRoleIsInvalid() {

        RegisterRequest req = mock(RegisterRequest.class);
        when(req.getEmail()).thenReturn("bad@corp.com");
        when(req.isRoleValid()).thenReturn(false);
        when(accountService.findByEmailOpt(any())).thenReturn(Optional.empty());


        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().contains("Role inválido"));
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentWhenRoleIsBranchAndBranchIsBlank")
    void shouldThrowIllegalArgumentWhenRoleIsBranchAndBranchIsBlank() {

        RegisterRequest req = new RegisterRequest();
        req.setEmail("branch@corp.com");
        req.setPassword("1234");
        req.setRole("BRANCH");
        req.setBranch(" "); // blank
        when(accountService.findByEmailOpt(req.getEmail())).thenReturn(Optional.empty());
        when(req.isRoleValid()).thenReturn(true);


        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().contains("Branch es obligatorio"));
        verify(accountService, never()).save(any());
    }
}
