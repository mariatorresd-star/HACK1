package com.example.hack1base.SalesAggregation.domain;


import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.Sale.estructure.SaleRepository;
import com.example.hack1base.salesaggregation.domain.SalesAggregates;
import com.example.hack1base.salesaggregation.domain.SalesAggregationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SalesAggregationServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private SalesAggregationService service;

    // ---------- Helpers ----------
    private Sale mkSale(int units, double price, String sku, String branch) {
        Sale s = mock(Sale.class);
        when(s.getUnits()).thenReturn(units);
        when(s.getPrice()).thenReturn(price);
        when(s.getSku()).thenReturn(sku);
        when(s.getBranch()).thenReturn(branch);
        return s;
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("Si from y to son null: usa últimos 7 días y consulta sin branch")
    void shouldUseLast7DaysWhenBothDatesNull() {

        LocalDate today = LocalDate.now();
        ArgumentCaptor<LocalDateTime> startCap = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCap   = ArgumentCaptor.forClass(LocalDateTime.class);


        List<Sale> ventas = List.of(
                mkSale(2, 100.0, "SKU-A", "Lima"),
                mkSale(3, 200.0, "SKU-B", "Surco")
        );
        when(saleRepository.findBySoldAtBetween(any(), any())).thenReturn(ventas);


        SalesAggregates out = service.calculateAggregates(null, null, null);


        verify(saleRepository).findBySoldAtBetween(startCap.capture(), endCap.capture());
        LocalDateTime esperadoInicio = today.minusDays(6).atStartOfDay();
        LocalDateTime esperadoFin    = today.atTime(LocalTime.MAX);

        assertEquals(esperadoInicio, startCap.getValue(), "start debe ser hoy-6 a las 00:00");
        assertEquals(esperadoFin, endCap.getValue(), "end debe ser hoy a las 23:59:59.999999999");

        assertAll(
                () -> assertEquals(5, out.getTotalUnits()),
                () -> assertEquals(2*100.0 + 3*200.0, out.getTotalRevenue()),
                () -> assertEquals("SKU-B", out.getTopSku()),   // 3 unidades > 2 unidades
                () -> assertEquals("Surco", out.getTopBranch()) // 3 unidades > 2 unidades
        );
    }

    @Test
    @DisplayName("Si uno de los dos (from/to) es null: lanza IllegalArgumentException")
    void shouldThrowWhenOneDateMissing() {

        LocalDate from = LocalDate.of(2025, 1, 1);


        assertThrows(IllegalArgumentException.class,
                () -> service.calculateAggregates(from, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateAggregates(null, LocalDate.of(2025, 1, 7), null));

        verifyNoInteractions(saleRepository);
    }

    @Test
    @DisplayName("Si to < from: lanza IllegalArgumentException")
    void shouldThrowWhenToBeforeFrom() {

        LocalDate from = LocalDate.of(2025, 1, 10);
        LocalDate to   = LocalDate.of(2025, 1, 5);


        assertThrows(IllegalArgumentException.class,
                () -> service.calculateAggregates(from, to, null));
        verifyNoInteractions(saleRepository);
    }

    @Test
    @DisplayName("Con fechas + branch en blanco: usa findBySoldAtBetween")
    void shouldCallBetweenWhenBranchBlank() {

        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to   = LocalDate.of(2025, 1, 7);
        when(saleRepository.findBySoldAtBetween(any(), any())).thenReturn(List.of());


        SalesAggregates out1 = service.calculateAggregates(from, to, null);
        SalesAggregates out2 = service.calculateAggregates(from, to, "   "); // blanco


        verify(saleRepository, times(2)).findBySoldAtBetween(any(), any());
        verify(saleRepository, never()).findByBranchAndSoldAtBetween(anyString(), any(), any());
        assertEquals(0, out1.getTotalUnits());
        assertEquals(0, out2.getTotalUnits());
    }

    @Test
    @DisplayName("Con fechas + branch no vacío: usa findByBranchAndSoldAtBetween")
    void shouldCallBranchBetweenWhenBranchProvided() {

        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to   = LocalDate.of(2025, 1, 7);
        String branch  = "Miraflores";
        when(saleRepository.findByBranchAndSoldAtBetween(eq(branch), any(), any()))
                .thenReturn(List.of());


        SalesAggregates out = service.calculateAggregates(from, to, branch);


        verify(saleRepository).findByBranchAndSoldAtBetween(eq(branch), any(), any());
        verify(saleRepository, never()).findBySoldAtBetween(any(), any());
        assertEquals(0, out.getTotalUnits());
        assertEquals(0.0, out.getTotalRevenue());
        assertNull(out.getTopSku());
        assertNull(out.getTopBranch());
    }

    @Test
    @DisplayName("Cuando no hay ventas: retorna 0, 0.0, null, null")
    void shouldReturnZerosWhenNoSales() {

        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to   = LocalDate.of(2025, 1, 7);
        when(saleRepository.findBySoldAtBetween(any(), any())).thenReturn(List.of());


        SalesAggregates out = service.calculateAggregates(from, to, null);


        assertAll(
                () -> assertEquals(0, out.getTotalUnits()),
                () -> assertEquals(0.0, out.getTotalRevenue()),
                () -> assertNull(out.getTopSku()),
                () -> assertNull(out.getTopBranch())
        );
    }

    @Test
    @DisplayName("Calcula totales y resoluciones de empate: por unidades y luego alfabético")
    void shouldComputeTotalsAndTieBreakers() {

        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to   = LocalDate.of(2025, 1, 31);

        List<Sale> ventas = List.of(
                mkSale(5, 10.0, "SKU-A", "Miraflores"),
                mkSale(5, 12.0, "SKU-B", "Miraflores"),
                mkSale(2, 20.0, "SKU-C", "Surco")
        );
        when(saleRepository.findBySoldAtBetween(any(), any()))
                .thenReturn(ventas);


        SalesAggregates out = service.calculateAggregates(from, to, null);


        assertAll(
                () -> assertEquals(12, out.getTotalUnits()),
                () -> assertEquals(150.0, out.getTotalRevenue()),
                () -> assertEquals("SKU-A", out.getTopSku()),
                () -> assertEquals("Miraflores", out.getTopBranch())
        );
    }
}
