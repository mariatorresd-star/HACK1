package com.example.hack1base.JWT.application;

import com.example.hack1base.JWT.domain.Account;
import com.example.hack1base.JWT.services.AuthService;
import com.example.hack1base.JWT.web.AccountDto;
import com.example.hack1base.JWT.web.AuthResponse;
import com.example.hack1base.JWT.web.LoginRequest;
import com.example.hack1base.JWT.web.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AccountDto> register(@Valid @RequestBody RegisterRequest req) {
        Account a = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountDto.from(a));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse resp = authService.login(req);
        return ResponseEntity.ok(resp);
    }
}