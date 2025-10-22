package com.example.hack1base.JWT.services;

import com.example.hack1base.JWT.domain.Account;
import com.example.hack1base.JWT.security.JwtService;
import com.example.hack1base.JWT.web.AuthResponse;
import com.example.hack1base.JWT.web.LoginRequest;
import com.example.hack1base.JWT.web.RegisterRequest;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
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
            throw new IllegalArgumentException("Email ya registrado");
        }
        Account a = new Account();
        a.setEmail(req.getEmail());
        a.setPassword(passwordEncoder.encode(req.getPassword()));
        a.setRole(req.getRole());
        a.setBranch(req.getBranch());
        return accountService.save(a);
    }
}
