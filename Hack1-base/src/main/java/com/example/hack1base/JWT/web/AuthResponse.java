package com.example.hack1base.JWT.web;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private final String token;
    private final long expiresAtMillis;
    private final String role;
    private final String branch;
}
