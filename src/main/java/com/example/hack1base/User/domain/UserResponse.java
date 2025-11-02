package com.example.hack1base.User.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String role;
    private String branch;
    private LocalDateTime createdAt;
}
