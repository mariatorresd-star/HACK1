package com.example.hack1base.JWT.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    @Email(message = "Email inválido")
    @NotBlank(message = "Email es obligatorio")
    private String email;

    @NotBlank(message = "Password es obligatorio")
    @Size(min = 8, message = "Password debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "Role es obligatorio")
    private String role;

    private String branch;

    @JsonIgnore
    public boolean isRoleValid() {
        return "CENTRAL".equalsIgnoreCase(role) || "BRANCH".equalsIgnoreCase(role);
    }
}
