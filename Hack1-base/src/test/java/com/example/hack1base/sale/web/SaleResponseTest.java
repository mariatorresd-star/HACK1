package com.example.hack1base.sale.web;


import com.example.hack1base.Sale.web.SaleResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SaleResponseTest {

    @Test
    @DisplayName("AllArgsConstructor: asigna correctamente todos los campos")
    void shouldCreateWithAllArgsConstructor() {

        Long id = 1L;
        String sku = "SKU-100";
        int units = 5;
        double price = 19.99;
        String branch = "Miraflores";
        LocalDateTime soldAt = LocalDateTime.of(2025, 1, 10, 12, 0);
        String createdBy = "adminUser";
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 10, 12, 5);


        SaleResponse response = new SaleResponse(
                id, sku, units, price, branch, soldAt, createdBy, createdAt
        );


        assertAll(
                () -> assertEquals(id, response.getId()),
                () -> assertEquals(sku, response.getSku()),
                () -> assertEquals(units, response.getUnits()),
                () -> assertEquals(price, response.getPrice()),
                () -> assertEquals(branch, response.getBranch()),
                () -> assertEquals(soldAt, response.getSoldAt()),
                () -> assertEquals(createdBy, response.getCreatedBy()),
                () -> assertEquals(createdAt, response.getCreatedAt())
        );
    }

    @Test
    @DisplayName("NoArgsConstructor + setters: permite asignar valores correctamente")
    void shouldAllowMutationWithSetters() {

        SaleResponse response = new SaleResponse();
        LocalDateTime soldAt = LocalDateTime.now();
        LocalDateTime createdAt = LocalDateTime.now();

        response.setId(2L);
        response.setSku("SKU-XYZ");
        response.setUnits(10);
        response.setPrice(50.5);
        response.setBranch("San Isidro");
        response.setSoldAt(soldAt);
        response.setCreatedBy("userTest");
        response.setCreatedAt(createdAt);

        assertAll(
                () -> assertEquals(2L, response.getId()),
                () -> assertEquals("SKU-XYZ", response.getSku()),
                () -> assertEquals(10, response.getUnits()),
                () -> assertEquals(50.5, response.getPrice()),
                () -> assertEquals("San Isidro", response.getBranch()),
                () -> assertEquals(soldAt, response.getSoldAt()),
                () -> assertEquals("userTest", response.getCreatedBy()),
                () -> assertEquals(createdAt, response.getCreatedAt())
        );
    }

    @Test
    @DisplayName("Valores límite: acepta 0 en units y price, y null en strings y fechas")
    void shouldAcceptBoundaryAndNullValues() {

        SaleResponse response = new SaleResponse(
                null, null, 0, 0.0, null, null, null, null
        );

        assertAll(
                () -> assertNull(response.getId()),
                () -> assertNull(response.getSku()),
                () -> assertEquals(0, response.getUnits()),
                () -> assertEquals(0.0, response.getPrice()),
                () -> assertNull(response.getBranch()),
                () -> assertNull(response.getSoldAt()),
                () -> assertNull(response.getCreatedBy()),
                () -> assertNull(response.getCreatedAt())
        );
    }
}

