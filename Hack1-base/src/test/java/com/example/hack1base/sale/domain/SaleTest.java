package com.example.hack1base.sale.domain;

import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.User.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SaleTest {

    @Test
    @DisplayName("AllArgsConstructor: asigna correctamente todos los campos")
    void shouldCreateWithAllArgsConstructor() {

        Long id = 1L;
        String sku = "SKU-123";
        int units = 4;
        double price = 19.99;
        String branch = "Miraflores";
        LocalDateTime soldAt = LocalDateTime.of(2025, 1, 10, 14, 30, 0);
        User createdBy = mock(User.class);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 10, 14, 31, 0);


        Sale sale = new Sale(id, sku, units, price, branch, soldAt, createdBy, createdAt);


        assertAll(
                () -> assertEquals(id, sale.getId()),
                () -> assertEquals(sku, sale.getSku()),
                () -> assertEquals(units, sale.getUnits()),
                () -> assertEquals(price, sale.getPrice()),
                () -> assertEquals(branch, sale.getBranch()),
                () -> assertEquals(soldAt, sale.getSoldAt()),
                () -> assertSame(createdBy, sale.getCreatedBy()),
                () -> assertEquals(createdAt, sale.getCreatedAt())
        );
    }

    @Test
    @DisplayName("NoArgsConstructor + setters: permite mutar valores correctamente")
    void shouldAllowMutationWithSetters() {

        Sale sale = new Sale();


        sale.setId(2L);
        sale.setSku("SKU-XYZ");
        sale.setUnits(10);
        sale.setPrice(25.5);
        sale.setBranch("San Isidro");
        LocalDateTime soldAt = LocalDateTime.of(2025, 2, 1, 9, 0);
        sale.setSoldAt(soldAt);
        User createdBy = mock(User.class);
        sale.setCreatedBy(createdBy);
        LocalDateTime createdAt = LocalDateTime.of(2025, 2, 1, 9, 5);
        sale.setCreatedAt(createdAt);


        assertAll(
                () -> assertEquals(2L, sale.getId()),
                () -> assertEquals("SKU-XYZ", sale.getSku()),
                () -> assertEquals(10, sale.getUnits()),
                () -> assertEquals(25.5, sale.getPrice()),
                () -> assertEquals("San Isidro", sale.getBranch()),
                () -> assertEquals(soldAt, sale.getSoldAt()),
                () -> assertSame(createdBy, sale.getCreatedBy()),
                () -> assertEquals(createdAt, sale.getCreatedAt())
        );
    }

    @Test
    @DisplayName("NoArgsConstructor: createdAt se inicializa automáticamente (no null y reciente)")
    void shouldInitializeCreatedAtAutomatically() {

        LocalDateTime before = LocalDateTime.now().minusSeconds(3);


        Sale sale = new Sale();


        assertNotNull(sale.getCreatedAt(), "createdAt no debe ser null");
        LocalDateTime after = LocalDateTime.now().plusSeconds(3);
        assertTrue(
                !sale.getCreatedAt().isBefore(before) && !sale.getCreatedAt().isAfter(after),
                "createdAt debe estar cerca del 'now' de creación"
        );
    }

    @Test
    @DisplayName("Permite valores límite coherentes (unidades 0, precio 0.0)")
    void shouldAcceptBoundaryValues() {

        Sale sale = new Sale();


        sale.setUnits(0);
        sale.setPrice(0.0);


        assertAll(
                () -> assertEquals(0, sale.getUnits()),
                () -> assertEquals(0.0, sale.getPrice())
        );
    }
}
