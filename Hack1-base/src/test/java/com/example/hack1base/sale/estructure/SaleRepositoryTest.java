package com.example.hack1base.sale.estructure;

import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.Sale.estructure.SaleRepository;
import com.example.hack1base.User.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleRepositoryTest {


    private Sale mkSale(String sku, int units, double price, String branch, LocalDateTime soldAt) {
        Sale s = new Sale();
        s.setSku(sku);
        s.setUnits(units);
        s.setPrice(price);
        s.setBranch(branch);
        s.setSoldAt(soldAt);
        s.setCreatedBy(mock(User.class));
        return s;
    }

    @Test
    @DisplayName("findBySoldAtBetween (lista): retorna ventas en rango de fechas")
    void shouldReturnListBetweenDates() {

        SaleRepository repo = mock(SaleRepository.class);
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end   = LocalDateTime.of(2025, 1, 31, 23, 59);
        List<Sale> expected = List.of(
                mkSale("A", 1, 10.0, "Miraflores", start.plusDays(1)),
                mkSale("B", 2, 20.0, "Surco", start.plusDays(2))
        );
        when(repo.findBySoldAtBetween(start, end)).thenReturn(expected);


        List<Sale> out = repo.findBySoldAtBetween(start, end);

        verify(repo).findBySoldAtBetween(start, end);
        assertEquals(expected, out);
    }

    @Test
    @DisplayName("findByBranchAndSoldAtBetween (lista): filtra por branch y fechas")
    void shouldReturnListByBranchAndBetween() {

        SaleRepository repo = mock(SaleRepository.class);
        String branch = "Miraflores";
        LocalDateTime start = LocalDateTime.of(2025, 2, 1, 0, 0);
        LocalDateTime end   = LocalDateTime.of(2025, 2, 28, 23, 59);
        List<Sale> expected = List.of(mkSale("X", 3, 15.0, branch, start.plusHours(5)));
        when(repo.findByBranchAndSoldAtBetween(branch, start, end)).thenReturn(expected);


        List<Sale> out = repo.findByBranchAndSoldAtBetween(branch, start, end);


        verify(repo).findByBranchAndSoldAtBetween(branch, start, end);
        assertEquals(expected, out);
    }

    @Test
    @DisplayName("findBySoldAtBetween (paginado): retorna Page con sort")
    void shouldReturnPageBetweenDates() {

        SaleRepository repo = mock(SaleRepository.class);
        LocalDateTime start = LocalDateTime.of(2025, 3, 1, 0, 0);
        LocalDateTime end   = LocalDateTime.of(2025, 3, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("soldAt").descending());
        Page<Sale> expected = new PageImpl<>(
                List.of(mkSale("P", 1, 9.9, "Lima", start.plusDays(3))),
                pageable, 1
        );
        when(repo.findBySoldAtBetween(start, end, pageable)).thenReturn(expected);


        Page<Sale> out = repo.findBySoldAtBetween(start, end, pageable);


        verify(repo).findBySoldAtBetween(start, end, pageable);
        assertSame(expected, out);
        assertEquals(1, out.getTotalElements());
    }

    @Test
    @DisplayName("findByBranchAndSoldAtBetween (paginado): combina branch + rango")
    void shouldReturnPageByBranchAndBetween() {

        SaleRepository repo = mock(SaleRepository.class);
        String branch = "Surco";
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime end   = LocalDateTime.of(2025, 4, 30, 23, 59);
        Pageable pageable = PageRequest.of(1, 5);
        Page<Sale> expected = new PageImpl<>(List.of(), pageable, 0);
        when(repo.findByBranchAndSoldAtBetween(branch, start, end, pageable)).thenReturn(expected);


        Page<Sale> out = repo.findByBranchAndSoldAtBetween(branch, start, end, pageable);

        verify(repo).findByBranchAndSoldAtBetween(branch, start, end, pageable);
        assertSame(expected, out);
    }

    @Test
    @DisplayName("findByBranch (paginado): filtra solo por branch")
    void shouldReturnPageByBranch() {

        SaleRepository repo = mock(SaleRepository.class);
        String branch = "Lince";
        Pageable pageable = PageRequest.of(0, 20);
        Page<Sale> expected = new PageImpl<>(List.of(), pageable, 0);
        when(repo.findByBranch(branch, pageable)).thenReturn(expected);

        Page<Sale> out = repo.findByBranch(branch, pageable);

        verify(repo).findByBranch(branch, pageable);
        assertSame(expected, out);
    }
}

