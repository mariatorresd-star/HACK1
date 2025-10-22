package com.example.hack1base.Sale.domain;

import com.example.hack1base.Sale.estructure.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;

    // Crear una venta
    public Sale createSale(Sale sale) {
        return saleRepository.save(sale);
    }

    // Obtener todas las ventas
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    // Buscar venta por ID
    public Optional<Sale> getSaleById(Long id) {
        return saleRepository.findById(id);
    }

    // Actualizar una venta
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
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    // Eliminar una venta
    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }
}