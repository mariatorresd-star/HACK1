package com.example.hack1base.salesaggregation.domain;

import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.Sale.estructure.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class SalesAggregationServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private SalesAggregationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SalesAggregationService(saleRepository);
    }

    private Sale sale(String sku, int units, double price, String branch, LocalDateTime soldAt) {
        Sale s = new Sale();
        s.setSku(sku);
        s.setUnits(units);
        s.setPrice(price);
        s.setBranch(branch);
        s.setSoldAt(soldAt);
        return s;
    }

    @Test
    void shouldCalculateCorrectAggregatesWithValidData() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();
        List<Sale> mockSales = List.of(
                sale("OREO_CLASSIC", 10, 1.99, "Miraflores", LocalDateTime.now().minusDays(6)),
                sale("OREO_DOUBLE", 5, 2.49, "San Isidro", LocalDateTime.now().minusDays(5)),
                sale("OREO_CLASSIC", 15, 1.99, "Miraflores", LocalDateTime.now().minusDays(3))
        );
        when(saleRepository.findBySoldAtBetween(any(), any())).thenReturn(mockSales);

        SalesAggregates result = service.calculateAggregates(from, to, null);

        assertThat(result.getTotalUnits()).isEqualTo(30);
        assertThat(result.getTotalRevenue()).isEqualTo(10*1.99 + 5*2.49 + 15*1.99);
        assertThat(result.getTopSku()).isEqualTo("OREO_CLASSIC");
        assertThat(result.getTopBranch()).isEqualTo("Miraflores");
    }

    @Test
    void shouldReturnZerosWhenNoSales() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();
        when(saleRepository.findBySoldAtBetween(any(), any())).thenReturn(List.of());

        SalesAggregates result = service.calculateAggregates(from, to, null);

        assertThat(result.getTotalUnits()).isEqualTo(0);
        assertThat(result.getTotalRevenue()).isEqualTo(0.0);
        assertThat(result.getTopSku()).isNull();
        assertThat(result.getTopBranch()).isNull();
    }

    @Test
    void shouldFilterByBranch() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();
        List<Sale> mockSales = List.of(
                sale("OREO_CLASSIC", 10, 1.99, "Miraflores", LocalDateTime.now().minusDays(6)),
                sale("OREO_DOUBLE", 5, 2.49, "San Isidro", LocalDateTime.now().minusDays(5))
        );
        when(saleRepository.findByBranchAndSoldAtBetween(eq("Miraflores"), any(), any())).thenReturn(mockSales.stream().filter(s -> s.getBranch().equals("Miraflores")).toList());

        SalesAggregates result = service.calculateAggregates(from, to, "Miraflores");

        assertThat(result.getTopBranch()).isEqualTo("Miraflores");
        assertThat(result.getTopSku()).isEqualTo("OREO_CLASSIC");
        assertThat(result.getTotalUnits()).isEqualTo(10);
    }

    @Test
    void shouldFilterByDates() {
        LocalDate from = LocalDate.now().minusDays(4);
        LocalDate to = LocalDate.now();
        List<Sale> mockSales = List.of(
                sale("OREO_CLASSIC", 10, 1.99, "Miraflores", LocalDateTime.now().minusDays(6)),
                sale("OREO_DOUBLE", 5, 2.49, "San Isidro", LocalDateTime.now().minusDays(2))
        );
        when(saleRepository.findBySoldAtBetween(any(), any())).thenReturn(mockSales.stream().filter(s -> s.getSoldAt().isAfter(LocalDateTime.now().minusDays(4))).toList());

        SalesAggregates result = service.calculateAggregates(from, to, null);

        assertThat(result.getTotalUnits()).isEqualTo(5);
        assertThat(result.getTopSku()).isEqualTo("OREO_DOUBLE");
        assertThat(result.getTopBranch()).isEqualTo("San Isidro");
    }

    @Test
    void shouldResolveTopSkuWithTies() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();
        List<Sale> mockSales = List.of(
                sale("OREO_A", 10, 2.0, "A", LocalDateTime.now().minusDays(3)),
                sale("OREO_B", 10, 2.0, "B", LocalDateTime.now().minusDays(2))
        );
        when(saleRepository.findBySoldAtBetween(any(), any())).thenReturn(mockSales);

        SalesAggregates result = service.calculateAggregates(from, to, null);
        // Empate por unidades; por orden alfabético se queda OREO_A
        assertThat(result.getTopSku()).isEqualTo("OREO_A");
    }
}
