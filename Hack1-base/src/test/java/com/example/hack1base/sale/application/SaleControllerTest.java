package com.example.hack1base.sale.application;

import com.example.hack1base.Exceptions.ForbiddenException;
import com.example.hack1base.Exceptions.ResourceNotFoundException;
import com.example.hack1base.Exceptions.UnauthorizedException;
import com.example.hack1base.JWT.domain.Account;
import com.example.hack1base.Sale.application.SaleController;
import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.Sale.domain.SaleService;
import com.example.hack1base.Sale.web.SaleRequest;
import com.example.hack1base.Sale.web.SaleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SaleController.class)
@Import(SaleControllerTest.ControllerTestAdvice.class)
class SaleControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockitoBean
    private SaleService saleService;

    @MockitoBean
    private ModelMapper modelMapper;

    // ---------- Advice para mapear excepciones a HTTP ----------
    @RestControllerAdvice
    static class ControllerTestAdvice {
        @ExceptionHandler(UnauthorizedException.class)
        public org.springframework.http.ResponseEntity<String> h401(UnauthorizedException ex) {
            return org.springframework.http.ResponseEntity.status(401).body(ex.getMessage());
        }
        @ExceptionHandler(ForbiddenException.class)
        public org.springframework.http.ResponseEntity<String> h403(ForbiddenException ex) {
            return org.springframework.http.ResponseEntity.status(403).body(ex.getMessage());
        }
        @ExceptionHandler(ResourceNotFoundException.class)
        public org.springframework.http.ResponseEntity<String> h404(ResourceNotFoundException ex) {
            return org.springframework.http.ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @BeforeEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    private Authentication mockAuthWith(Account account) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(account);
        return auth;
    }

    private void setSecurity(Account account) {
        var ctx = mock(org.springframework.security.core.context.SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(account == null ? null : mockAuthWith(account));
        SecurityContextHolder.setContext(ctx);
    }

    private Sale mkSale(Long id, String sku, int units, double price, String branch, LocalDateTime soldAt) {
        Sale s = new Sale();
        s.setId(id);
        s.setSku(sku);
        s.setUnits(units);
        s.setPrice(price);
        s.setBranch(branch);
        s.setSoldAt(soldAt);
        return s;
    }

    // ========== TESTS ==========

    @Test
    @DisplayName("should allow CENTRAL to create sale and return 200 OK")
    void shouldCreateSaleAsCentralReturnOk() throws Exception {
        // Arrange
        Account acc = mock(Account.class);
        when(acc.getRole()).thenReturn("CENTRAL");
        setSecurity(acc);

        SaleRequest req = new SaleRequest();
        req.setSku("SKU-1");
        req.setUnits(3);
        req.setPrice(10.0);
        req.setBranch("Miraflores");
        req.setSoldAt(LocalDateTime.of(2025, 1, 10, 12, 0));

        Sale entityToCreate = mkSale(null, "SKU-1", 3, 10.0, "Miraflores", req.getSoldAt());
        Sale created = mkSale(100L, "SKU-1", 3, 10.0, "Miraflores", req.getSoldAt());
        SaleResponse resp = new SaleResponse(
                100L, "SKU-1", 3, 10.0, "Miraflores",
                req.getSoldAt(), "userX", LocalDateTime.of(2025, 1, 10, 12, 5)
        );

        when(modelMapper.map(any(SaleRequest.class), eq(Sale.class))).thenReturn(entityToCreate);
        when(saleService.createSale(entityToCreate)).thenReturn(created);
        when(modelMapper.map(created, SaleResponse.class)).thenReturn(resp);

        mvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.sku", is("SKU-1")))
                .andExpect(jsonPath("$.units", is(3)))
                .andExpect(jsonPath("$.branch", is("Miraflores")));
    }

    @Test
    @DisplayName("should forbid BRANCH from creating in a different branch (403)")
    void shouldForbidBranchCreatingInAnotherBranch() throws Exception {

        Account acc = mock(Account.class);
        when(acc.getRole()).thenReturn("BRANCH");
        when(acc.getBranch()).thenReturn("Surco");
        setSecurity(acc);

        SaleRequest req = new SaleRequest();
        req.setSku("SKU-1");
        req.setUnits(1);
        req.setPrice(5.0);
        req.setBranch("Miraflores");
        req.setSoldAt(LocalDateTime.now());

        mvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(saleService);
    }

    @Test
    @DisplayName("should force Account branch when role is BRANCH on GET /api/sales")
    void shouldForceAccountBranchWhenRoleIsBranchOnGetAllSales() throws Exception {

        Account acc = mock(Account.class);
        when(acc.getRole()).thenReturn("BRANCH");
        when(acc.getBranch()).thenReturn("Surco");
        setSecurity(acc);

        var start = LocalDate.of(2025, 1, 1).atStartOfDay();
        var end = LocalDate.of(2025, 1, 31).atTime(LocalTime.MAX);

        Pageable pageable = PageRequest.of(0, 10);
        Sale s = mkSale(1L, "SKU-X", 2, 11.0, "Surco", start.plusDays(1));
        Page<Sale> page = new PageImpl<>(java.util.List.of(s), pageable, 1);
        when(saleService.findSales(eq(start), eq(end), any(String.class), eq(pageable))).thenReturn(page);

        SaleResponse mapped = new SaleResponse(1L, "SKU-X", 2, 11.0, "Surco",
                s.getSoldAt(), "userX", LocalDateTime.now());
        when(modelMapper.map(eq(s), eq(SaleResponse.class))).thenReturn(mapped);

        mvc.perform(get("/api/sales")
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-31")
                        .param("branch", "Miraflores")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].branch", is("Surco")))
                .andExpect(jsonPath("$.totalElements", is(1)));

        ArgumentCaptor<String> branchCap = ArgumentCaptor.forClass(String.class);
        verify(saleService).findSales(eq(start), eq(end), branchCap.capture(), eq(pageable));
        assertEquals("Surco", branchCap.getValue(), "El controller debe forzar el branch del Account");
    }

    @Test
    @DisplayName("should return 404 Not Found when sale does not exist")
    void shouldReturnNotFoundWhenSaleDoesNotExist() throws Exception {

        Account acc = mock(Account.class);
        when(acc.getRole()).thenReturn("CENTRAL");
        setSecurity(acc);

        when(saleService.getSaleById(999L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/sales/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should parse from/to into startOfDay and endOfDay")
    void shouldParseFromToAsStartAndEndOfDay() throws Exception {

        Account acc = mock(Account.class);
        when(acc.getRole()).thenReturn("CENTRAL");
        setSecurity(acc);

        LocalDateTime expectedStart = LocalDate.parse("2025-05-01").atStartOfDay();
        LocalDateTime expectedEnd   = LocalDate.parse("2025-05-15").atTime(LocalTime.MAX);

        Pageable pageable = PageRequest.of(1, 5);
        Page<Sale> page = new PageImpl<>(java.util.List.of(), pageable, 0);
        when(saleService.findSales(any(), any(), any(), any())).thenReturn(page);

        mvc.perform(get("/api/sales")
                        .param("from", "2025-05-01")
                        .param("to", "2025-05-15")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<LocalDateTime> startCap = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCap = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(saleService).findSales(startCap.capture(), endCap.capture(), any(), eq(pageable));
        assertEquals(expectedStart, startCap.getValue());
        assertEquals(expectedEnd, endCap.getValue());
    }
}
