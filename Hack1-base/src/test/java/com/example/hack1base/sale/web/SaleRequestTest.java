package com.example.hack1base.sale.web;


import com.example.hack1base.Sale.web.SaleRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SaleRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ---------- Tests de validación ----------

    @Test
    @DisplayName("Debe ser válido cuando todos los campos cumplen las restricciones")
    void shouldBeValidWhenAllFieldsCorrect() {
        // Arrange
        SaleRequest req = new SaleRequest();
        req.setSku("SKU-001");
        req.setUnits(5);
        req.setPrice(20.5);
        req.setBranch("Miraflores");
        req.setSoldAt(LocalDateTime.now());

        Set<ConstraintViolation<SaleRequest>> violations = validator.validate(req);

        assertTrue(violations.isEmpty(), "No debe haber violaciones cuando los datos son válidos");
    }

    @Test
    @DisplayName("Debe fallar cuando SKU es nulo o vacío")
    void shouldFailWhenSkuBlank() {

        SaleRequest req = new SaleRequest();
        req.setSku("   ");
        req.setUnits(5);
        req.setPrice(10.0);
        req.setBranch("Lima");
        req.setSoldAt(LocalDateTime.now());

        Set<ConstraintViolation<SaleRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("sku")));
    }

    @Test
    @DisplayName("Debe fallar cuando units o price son menores que 1")
    void shouldFailWhenUnitsOrPriceBelowMin() {

        SaleRequest req = new SaleRequest();
        req.setSku("SKU-X");
        req.setUnits(0);
        req.setPrice(0.5);
        req.setBranch("Lima");
        req.setSoldAt(LocalDateTime.now());


        Set<ConstraintViolation<SaleRequest>> violations = validator.validate(req);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("units")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    @Test
    @DisplayName("Debe fallar cuando branch es nulo o vacío")
    void shouldFailWhenBranchBlank() {

        SaleRequest req = new SaleRequest();
        req.setSku("SKU-777");
        req.setUnits(2);
        req.setPrice(9.99);
        req.setBranch("");
        req.setSoldAt(LocalDateTime.now());

        Set<ConstraintViolation<SaleRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("branch")));
    }

    @Test
    @DisplayName("Debe fallar cuando soldAt es nulo")
    void shouldFailWhenSoldAtIsNull() {

        SaleRequest req = new SaleRequest();
        req.setSku("SKU-888");
        req.setUnits(3);
        req.setPrice(15.0);
        req.setBranch("San Isidro");
        req.setSoldAt(null);

        Set<ConstraintViolation<SaleRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("soldAt")));
    }

    // ---------- Tests de getters y setters ----------

    @Test
    @DisplayName("Debe asignar y recuperar correctamente los valores con getters/setters")
    void shouldUseGettersAndSettersCorrectly() {

        SaleRequest req = new SaleRequest();
        LocalDateTime now = LocalDateTime.now();

        req.setSku("SKU-TEST");
        req.setUnits(10);
        req.setPrice(99.99);
        req.setBranch("Surco");
        req.setSoldAt(now);

        assertAll(
                () -> assertEquals("SKU-TEST", req.getSku()),
                () -> assertEquals(10, req.getUnits()),
                () -> assertEquals(99.99, req.getPrice()),
                () -> assertEquals("Surco", req.getBranch()),
                () -> assertEquals(now, req.getSoldAt())
        );
    }
}

