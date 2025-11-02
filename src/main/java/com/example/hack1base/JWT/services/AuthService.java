package com.example.hack1base.JWT.services;

import com.example.hack1base.JWT.domain.Account;
import com.example.hack1base.JWT.security.JwtService;
import com.example.hack1base.JWT.web.AuthResponse;
import com.example.hack1base.JWT.web.LoginRequest;
import com.example.hack1base.JWT.web.RegisterRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authManager,
                       AccountService accountService,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.authManager = authManager;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        Account account = (Account) auth.getPrincipal();
        String token = jwtService.generateToken(account.getEmail(), account.getRole(), account.getBranch());
        return new AuthResponse(token, jwtService.isTokenExpired(token) ? 0L : jwtService.extractAllClaims(token).getExpiration().getTime(), account.getRole(), account.getBranch());
    }

    public Account register(RegisterRequest req) {
        if (accountService.findByEmailOpt(req.getEmail()).isPresent()) {
            throw new com.example.hack1base.Exceptions.ConflictException("Email ya registrado");
        }
        if (!req.isRoleValid()) {
            throw new IllegalArgumentException("Role inválido. Debe ser CENTRAL o BRANCH");
        }
        if ("BRANCH".equalsIgnoreCase(req.getRole()) && (req.getBranch() == null || req.getBranch().isBlank())) {
            throw new IllegalArgumentException("Branch es obligatorio cuando el role es BRANCH");
        }
        Account a = new Account();
        a.setEmail(req.getEmail());
        a.setPassword(passwordEncoder.encode(req.getPassword()));
        a.setRole(req.getRole());
        a.setBranch(req.getBranch());
        return accountService.save(a);
    }
}
