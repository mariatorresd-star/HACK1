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

class RegisterRequestTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // =============== Casos válidos ===============

    @Test
    @DisplayName("shouldBeValidWhenCentralRoleAndFieldsAreCorrect")
    void shouldBeValidWhenCentralRoleAndFieldsAreCorrect() {

        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@corp.com");
        req.setPassword("12345678");
        req.setRole("CENTRAL");


        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);


        assertTrue(violations.isEmpty());
        assertTrue(req.isRoleValid());
    }

    @Test
    @DisplayName("shouldBeValidWhenBranchRoleAndFieldsAreCorrect")
    void shouldBeValidWhenBranchRoleAndFieldsAreCorrect() {

        RegisterRequest req = new RegisterRequest();
        req.setEmail("branch@corp.com");
        req.setPassword("abcdefgh");
        req.setRole("BRANCH");
        req.setBranch("Miraflores");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);


        assertTrue(violations.isEmpty());
        assertTrue(req.isRoleValid());
    }

    // =============== Casos inválidos por anotaciones ===============

    @Test
    @DisplayName("shouldFailValidationWhenEmailIsBlank")
    void shouldFailValidationWhenEmailIsBlank() {

        RegisterRequest req = new RegisterRequest();
        req.setEmail("   ");
        req.setPassword("12345678");
        req.setRole("CENTRAL");


        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("shouldFailValidationWhenEmailHasInvalidFormat")
    void shouldFailValidationWhenEmailHasInvalidFormat() {

        RegisterRequest req = new RegisterRequest();
        req.setEmail("not-an-email");
        req.setPassword("12345678");
        req.setRole("CENTRAL");


        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("shouldFailValidationWhenPasswordIsBlank")
    void shouldFailValidationWhenPasswordIsBlank() {

        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@corp.com");
        req.setPassword("   ");
        req.setRole("CENTRAL");


        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    @DisplayName("shouldFailValidationWhenPasswordIsTooShort")
    void shouldFailValidationWhenPasswordIsTooShort() {

        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@corp.com");
        req.setPassword("short"); // < 8
        req.setRole("CENTRAL");


        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    @DisplayName("shouldFailValidationWhenRoleIsBlank")
    void shouldFailValidationWhenRoleIsBlank() {

        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@corp.com");
        req.setPassword("validPass");
        req.setRole("   ");


        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    // =============== isRoleValid() ===============

    @Test
    @DisplayName("shouldReturnTrueWhenRoleIsCentralOrBranchIgnoringCase")
    void shouldReturnTrueWhenRoleIsCentralOrBranchIgnoringCase() {

        RegisterRequest r1 = new RegisterRequest(); r1.setRole("CENTRAL");
        RegisterRequest r2 = new RegisterRequest(); r2.setRole("central");
        RegisterRequest r3 = new RegisterRequest(); r3.setRole("BRANCH");
        RegisterRequest r4 = new RegisterRequest(); r4.setRole("branch");


        assertTrue(r1.isRoleValid());
        assertTrue(r2.isRoleValid());
        assertTrue(r3.isRoleValid());
        assertTrue(r4.isRoleValid());
    }

    @Test
    @DisplayName("shouldReturnFalseWhenRoleIsInvalid")
    void shouldReturnFalseWhenRoleIsInvalid() {

        RegisterRequest r1 = new RegisterRequest(); r1.setRole("ADMIN");
        RegisterRequest r2 = new RegisterRequest(); r2.setRole("");
        RegisterRequest r3 = new RegisterRequest(); r3.setRole(null);


        assertFalse(r1.isRoleValid());
        assertFalse(r2.isRoleValid());
        assertFalse(r3.isRoleValid());
    }

    // =============== Getters/Setters ===============

    @Test
    @DisplayName("shouldAssignAndReadFieldsWithGettersAndSetters")
    void shouldAssignAndReadFieldsWithGettersAndSetters() {

        RegisterRequest req = new RegisterRequest();


        req.setEmail("a@b.com");
        req.setPassword("12345678");
        req.setRole("BRANCH");
        req.setBranch("Surco");


        assertAll(
                () -> assertEquals("a@b.com", req.getEmail()),
                () -> assertEquals("12345678", req.getPassword()),
                () -> assertEquals("BRANCH", req.getRole()),
                () -> assertEquals("Surco", req.getBranch())
        );
    }
}
