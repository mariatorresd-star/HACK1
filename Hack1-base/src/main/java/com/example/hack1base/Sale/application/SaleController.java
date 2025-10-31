package com.example.hack1base.Sale.application;

import com.example.hack1base.JWT.domain.Account;
import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.Sale.domain.SaleService;
import com.example.hack1base.Sale.web.SaleRequest;
import com.example.hack1base.Sale.web.SaleResponse;
import com.example.hack1base.Exceptions.ForbiddenException;
import com.example.hack1base.Exceptions.ResourceNotFoundException;
import com.example.hack1base.Exceptions.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleRequest request) {
        Account current = getCurrentAccount();
        if (current == null) {
            throw new UnauthorizedException("No autenticado");
        }
        if ("BRANCH".equalsIgnoreCase(current.getRole())) {
            if (request.getBranch() == null || !request.getBranch().equalsIgnoreCase(current.getBranch())) {
                throw new ForbiddenException("No autorizado para crear ventas en otra sucursal");
            }
        }
        // Mapear DTO a entidad
        Sale toCreate = modelMapper.map(request, Sale.class);
        Sale created = saleService.createSale(toCreate);
        return ResponseEntity.ok(modelMapper.map(created, SaleResponse.class));
    }

    @GetMapping
    public ResponseEntity<Page<SaleResponse>> getAllSales(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String branch,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Account current = getCurrentAccount();
        if (current == null) throw new UnauthorizedException("No autenticado");

        // Si es BRANCH, forzar su branch
        if ("BRANCH".equalsIgnoreCase(current.getRole())) {
            branch = current.getBranch();
        }

        LocalDateTime start = null;
        LocalDateTime end = null;
        if (from != null && to != null) {
            LocalDate f = LocalDate.parse(from);
            LocalDate t = LocalDate.parse(to);
            start = f.atStartOfDay();
            end = t.atTime(LocalTime.MAX);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Sale> result = saleService.findSales(start, end, branch, pageable);
        Page<SaleResponse> mapped = result.map(s -> modelMapper.map(s, SaleResponse.class));
        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSaleById(@PathVariable Long id) {
        Account current = getCurrentAccount();
        if (current == null) throw new UnauthorizedException("No autenticado");

        Sale sale = saleService.getSaleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        if (!canAccessSale(current, sale)) {
            throw new ForbiddenException("No autorizado para acceder a esta venta");
        }
        return ResponseEntity.ok(modelMapper.map(sale, SaleResponse.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponse> updateSale(@PathVariable Long id, @Valid @RequestBody SaleRequest request) {
        Account current = getCurrentAccount();
        if (current == null) throw new UnauthorizedException("No autenticado");

        Sale existing = saleService.getSaleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        if (!canAccessSale(current, existing)) {
            throw new ForbiddenException("No autorizado para actualizar esta venta");
        }
        Sale updates = modelMapper.map(request, Sale.class);
        Sale updated = saleService.updateSale(id, updates);
        return ResponseEntity.ok(modelMapper.map(updated, SaleResponse.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        Account current = getCurrentAccount();
        if (current == null) throw new UnauthorizedException("No autenticado");
        // Solo CENTRAL puede eliminar
        if (!"CENTRAL".equalsIgnoreCase(current.getRole())) {
            throw new ForbiddenException("Solo CENTRAL puede eliminar ventas");
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
