package com.example.hack1base.JWT.web;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("shouldBeValidWhenEmailAndPasswordAreCorrect")
    void shouldBeValidWhenEmailAndPasswordAreCorrect() {

        LoginRequest req = new LoginRequest();
        req.setEmail("user@corp.com");
        req.setPassword("secret");


        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);


        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("shouldFailValidationWhenEmailIsBlank")
    void shouldFailValidationWhenEmailIsBlank() {

        LoginRequest req = new LoginRequest();
        req.setEmail("   ");
        req.setPassword("secret");


        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("shouldFailValidationWhenEmailIsInvalidFormat")
    void shouldFailValidationWhenEmailIsInvalidFormat() {

        LoginRequest req = new LoginRequest();
        req.setEmail("not-an-email");
        req.setPassword("secret");


        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("shouldFailValidationWhenPasswordIsBlank")
    void shouldFailValidationWhenPasswordIsBlank() {

        LoginRequest req = new LoginRequest();
        req.setEmail("user@corp.com");
        req.setPassword("   ");


        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    @DisplayName("shouldAssignAndReadFieldsWithGettersAndSetters")
    void shouldAssignAndReadFieldsWithGettersAndSetters() {

        LoginRequest req = new LoginRequest();


        req.setEmail("a@b.com");
        req.setPassword("123456");


        assertAll(
                () -> assertEquals("a@b.com", req.getEmail()),
                () -> assertEquals("123456", req.getPassword())
        );
    }
}

