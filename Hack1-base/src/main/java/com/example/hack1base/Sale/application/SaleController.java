package com.example.hack1base.Sale.application;

import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.Sale.domain.SaleService;
import com.example.hack1base.JWT.domain.Account;
import com.example.hack1base.common.web.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<Sale> createSale(@RequestBody Sale sale) {
        Account current = getCurrentAccount();
        if (current == null) {
            return ResponseEntity.status(401).build();
        }
        if ("BRANCH".equalsIgnoreCase(current.getRole())) {
            if (sale.getBranch() == null || !sale.getBranch().equalsIgnoreCase(current.getBranch())) {
                return ResponseEntity.status(403).build();
            }
        }
        sale.setCreatedBy(current);
        return ResponseEntity.ok(saleService.createSale(sale));
    }

    @GetMapping
    public ResponseEntity<List<Sale>> getAllSales() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSaleById(@PathVariable Long id) {
    Account current = getCurrentAccount();
    return saleService.getSaleById(id)
        .filter(s -> canAccessSale(current, s))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.status(403).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sale> updateSale(@PathVariable Long id, @RequestBody Sale sale) {
        Account current = getCurrentAccount();
        return saleService.getSaleById(id)
                .filter(s -> canAccessSale(current, s))
                .map(s -> ResponseEntity.ok(saleService.updateSale(id, sale)))
                .orElse(ResponseEntity.status(403).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        Account current = getCurrentAccount();
        // Solo CENTRAL puede eliminar
        if (current == null || !"CENTRAL".equalsIgnoreCase(current.getRole())) {
            return ResponseEntity.status(403).build();
        }
        saleService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }

    private Account getCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Account acc)) {
            return null;
        }
        return acc;
    }

    private boolean canAccessSale(Account acc, Sale sale) {
        if (acc == null) return false;
        if ("CENTRAL".equalsIgnoreCase(acc.getRole())) return true;
        return "BRANCH".equalsIgnoreCase(acc.getRole())
                && sale.getBranch() != null
                && acc.getBranch() != null
                && sale.getBranch().equalsIgnoreCase(acc.getBranch());
    }
}
