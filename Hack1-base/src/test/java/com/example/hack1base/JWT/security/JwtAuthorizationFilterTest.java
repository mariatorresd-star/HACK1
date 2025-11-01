package com.example.hack1base.JWT.security;


import com.example.hack1base.JWT.services.AccountService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthorizationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private JwtAuthorizationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe continuar la cadena cuando no hay Authorization header")
    void shouldContinueChainWhenNoAuthorizationHeader() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = spy(new MockFilterChain());


        filter.doFilter(request, response, chain);


        verify(chain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, accountService);
    }

    @Test
    @DisplayName("Debe setear autenticación cuando el token es válido")
    void shouldSetAuthenticationWhenTokenIsValid() throws Exception {

        String token = "valid.token";
        String username = "user@corp.com";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = spy(new MockFilterChain());

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);
        when(userDetails.getAuthorities()).thenReturn(java.util.List.of());

        when(jwtService.extractUsername(token)).thenReturn(username);
        when(accountService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, username)).thenReturn(true);


        filter.doFilter(request, response, chain);


        verify(chain, times(1)).doFilter(request, response);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Debe setear Authentication en el contexto");
        assertTrue(auth instanceof UsernamePasswordAuthenticationToken);
        assertEquals(userDetails, auth.getPrincipal());
    }

    @Test
    @DisplayName("Debe responder 401 JSON cuando el token está expirado")
    void shouldReturn401JsonWhenTokenExpired() throws Exception {

        String token = "expired.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        ExpiredJwtException expired = mock(ExpiredJwtException.class);
        when(jwtService.extractUsername(token)).thenThrow(expired);


        filter.doFilter(request, response, chain);


        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        String body = response.getContentAsString();
        assertTrue(body.contains("\"TOKEN_EXPIRED\""));
        assertTrue(body.contains("El token expiró."));
        verify(chain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Debe responder 401 JSON cuando el token es inválido")
    void shouldReturn401JsonWhenTokenInvalid() throws Exception {

        String token = "invalid.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtService.extractUsername(token)).thenThrow(new JwtException("bad token"));


        filter.doFilter(request, response, chain);


        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        String body = response.getContentAsString();
        assertTrue(body.contains("\"INVALID_TOKEN\""));
        assertTrue(body.contains("Token inválido."));
        verify(chain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("No debe setear auth cuando ya existe autenticación previa en el contexto")
    void shouldNotSetAuthenticationWhenAlreadyAuthenticated() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("already", null, java.util.List.of())
        );

        String token = "valid.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = spy(new MockFilterChain());

        when(jwtService.extractUsername(token)).thenReturn("user@corp.com");


        filter.doFilter(request, response, chain);


        verify(chain, times(1)).doFilter(request, response);
        verifyNoInteractions(accountService);
        verify(jwtService, times(1)).extractUsername(token);
        assertEquals("already",
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("No debe autenticar cuando isTokenValid retorna false")
    void shouldNotAuthenticateWhenTokenValidationFails() throws Exception {

        String token = "maybe.token";
        String username = "user@corp.com";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = spy(new MockFilterChain());

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);
        when(userDetails.getAuthorities()).thenReturn(java.util.List.of());

        when(jwtService.extractUsername(token)).thenReturn(username);
        when(accountService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, username)).thenReturn(false);


        filter.doFilter(request, response, chain);


        verify(chain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "No debe setear Authentication cuando el token no es válido");
    }
}
