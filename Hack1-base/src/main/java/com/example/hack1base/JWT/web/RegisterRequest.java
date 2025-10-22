package com.example.hack1base.JWT.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String role; // CENTRAL or BRANCH
    private String branch; // required if BRANCH
}
