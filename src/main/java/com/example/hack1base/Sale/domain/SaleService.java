package com.example.hack1base.Sale.domain;

import com.example.hack1base.Sale.estructure.SaleRepository;
import com.example.hack1base.Exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;

    public Sale createSale(Sale sale) {
        return saleRepository.save(sale);
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public Optional<Sale> getSaleById(Long id) {
        return saleRepository.findById(id);
    }

    public Sale updateSale(Long id, Sale saleDetails) {
        return saleRepository.findById(id)
                .map(sale -> {
                    sale.setSku(saleDetails.getSku());
                    sale.setUnits(saleDetails.getUnits());
                    sale.setPrice(saleDetails.getPrice());
                    sale.setBranch(saleDetails.getBranch());
                    sale.setSoldAt(saleDetails.getSoldAt());
                    sale.setCreatedBy(saleDetails.getCreatedBy());
                    return saleRepository.save(sale);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
    }

    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }

    public Page<Sale> findSales(LocalDateTime start, LocalDateTime end, String branch, Pageable pageable) {
        if (start != null && end != null) {
            if (branch == null || branch.isBlank()) {
                return saleRepository.findBySoldAtBetween(start, end, pageable);
            } else {
                return saleRepository.findByBranchAndSoldAtBetween(branch, start, end, pageable);
            }
        }
        if (branch != null && !branch.isBlank()) {
            return saleRepository.findByBranch(branch, pageable);
        }
        return saleRepository.findAll(pageable);
    }
}